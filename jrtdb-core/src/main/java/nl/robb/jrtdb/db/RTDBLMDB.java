package nl.robb.jrtdb.db;

import nl.robb.jrtdb.common.RTDBTimestamp;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.Env.create;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import org.lmdbjava.KeyRange;
import org.lmdbjava.Txn;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

/**
 *
 * @author rob
 */
class RTDBLMDB implements AutoCloseable {

    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> db;

    public RTDBLMDB(File path) {
        // We need a storage directory first.
        // The path cannot be on a remote file system.
        path.mkdirs(); // TODO: Check if path exists

        // We always need an Env. An Env owns a physical on-disk storage file. One
        // Env can store many different databases (ie sorted maps).
        env = create()
                // LMDB also needs to know how large our DB might be. Over-estimating is OK.
                .setMapSize(100 * 1024 * 1024)
                // LMDB also needs to know how many DBs (Dbi) we want to store in this Env.
                .setMaxDbs(1)
                // Now let's open the Env. The same path can be concurrently opened and
                // used in different processes, but do not open the same path twice in
                // the same process at the same time.
                .open(path, EnvFlags.MDB_WRITEMAP, EnvFlags.MDB_NOSYNC, EnvFlags.MDB_NOMETASYNC, EnvFlags.MDB_NOTLS);

        // We need a Dbi for each DB. A Dbi roughly equates to a sorted map. The
        // MDB_CREATE flag causes the DB to be created if it doesn't already exist.
        db = env.openDbi((String)null, MDB_CREATE);
    }

    @Override
    public void close() {
        env.close();
    }

    public RTDBItem get(String key) throws IOException {
        // We want to store some data, so we will need a direct ByteBuffer.
        // Note that LMDB keys cannot exceed maxKeySize bytes (511 bytes by default).
        // Values can be larger.
        final ByteBuffer dbkey = allocateDirect(env.getMaxKeySize());
        dbkey.put(key.getBytes(UTF_8)).flip();

        RTDBItem item = null;
        // To fetch any data from LMDB we need a Txn. A Txn is very important in
        // LmdbJava because it offers ACID characteristics and internally holds a
        // read-only key buffer and read-only value buffer. These read-only buffers
        // are always the same two Java objects, but point to different LMDB-managed
        // memory as we use Dbi (and Cursor) methods. These read-only buffers remain
        // valid only until the Txn is released or the next Dbi or Cursor call. If
        // you need data afterwards, you should copy the bytes to your own buffer.
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            final ByteBuffer found = db.get(txn, dbkey);

            // The fetchedVal is read-only and points to LMDB memory
            final ByteBuffer fetchedVal = txn.val();

            byte[] dbdata = new byte[fetchedVal.remaining()];
            fetchedVal.get(dbdata);

            item = toRTDBItem(dbdata);
        }
        return item;
    }

    public Map<String, RTDBItem> getAll() throws IOException {
        HashMap<String, RTDBItem> result = new HashMap<>();
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            // Each iterable uses a cursor and must be closed when finished. Iterate
            // forward in terms of key ordering starting with the first key.
            try (CursorIterable<ByteBuffer> ci = db.iterate(txn, KeyRange.all())) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    String key = UTF_8.decode(kv.key()).toString();

                    // The fetchedVal is read-only and points to LMDB memory
                    final ByteBuffer fetchedVal = kv.val();
                    byte[] dbdata = new byte[fetchedVal.remaining()];
                    fetchedVal.get(dbdata);

                    result.put(key, toRTDBItem(dbdata));
                }
            }
        }
        return result;
    }

    void put(String key, RTDBItem item) throws IOException {

        final ByteBuffer dbkey = allocateDirect(env.getMaxKeySize());
        final ByteBuffer dbval = allocateDirect(700);

        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            dbkey.put(key.getBytes(UTF_8)).flip();
            dbval.put(toByteArray(item)).flip();
            db.put(txn, dbkey, dbval);

            // An explicit commit is required, otherwise Txn.close() rolls it back.
            txn.commit();
        }
    }

    private RTDBItem toRTDBItem(byte[] dbdata) throws IOException {
// Preferred solution however decoding byte array does not give the expected result
//            ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
//            objectMapper.setAnnotationIntrospector(new JsonArrayFormat());
//            item = objectMapper.readValue(dbdata, RTDBItem.class);

        if (dbdata == null || dbdata.length == 0) {
            return null;
        }

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(dbdata, 0, dbdata.length);
        Value v = unpacker.unpackValue();
        ArrayValue av = v.asArrayValue();

        byte[] data = av.get(0).asStringValue().asByteArray();
        ArrayValue tsav = av.get(1).asArrayValue();
        RTDBTimestamp timestamp = new RTDBTimestamp(
            tsav.get(0).asIntegerValue().asInt(), tsav.get(1).asIntegerValue().asInt());
        boolean isShared = av.get(2).asBooleanValue().getBoolean();
        boolean isList = av.get(3).asBooleanValue().getBoolean();
        return new RTDBItem(data, timestamp, isShared, isList);
    }

    private byte[] toByteArray(RTDBItem item) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packArrayHeader(4);

        // data
        packer.packRawStringHeader(item.getData().length);
        packer.writePayload(item.getData());

        // timestamp
        packer.packArrayHeader(2);
        packer.packInt(item.getTimestamp().getTvSec());
        packer.packInt(item.getTimestamp().getTvUSec());

        // isShared
        packer.packBoolean(item.isShared());
        // isList
        packer.packBoolean(item.isList());

        return packer.toByteArray();
    }
}
