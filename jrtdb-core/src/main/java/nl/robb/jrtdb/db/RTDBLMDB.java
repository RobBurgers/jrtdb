package nl.robb.jrtdb.db;

import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.Env.create;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.EnvFlags;
import org.lmdbjava.Txn;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;

/**
 *
 * @author rob
 */
class RTDBLMDB {

    private final File path;

    public RTDBLMDB(File path) {
        // We need a storage directory first.
        // The path cannot be on a remote file system.
        this.path = path;
    }

    public RTDBItem get(String key) throws IOException {
        // TODO: Check if path exists
        path.mkdirs();

        // We always need an Env. An Env owns a physical on-disk storage file. One
        // Env can store many different databases (ie sorted maps).
        final Env<ByteBuffer> env = create()
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
        final Dbi<ByteBuffer> db = env.openDbi((String)null, MDB_CREATE);

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

// Preferred solution however decoding byte array does not give the expected result
//            ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
//            objectMapper.setAnnotationIntrospector(new JsonArrayFormat());
//            item = objectMapper.readValue(dbdata, RTDBItem.class);

            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(dbdata, 0, dbdata.length);
            Value v = unpacker.unpackValue();
            ArrayValue av = v.asArrayValue();

            byte[] data = av.get(0).asStringValue().asByteArray();
            ArrayValue tsav = av.get(1).asArrayValue();
            RTDBTimestamp timestamp = new RTDBTimestamp(
                tsav.get(0).asIntegerValue().asInt(), tsav.get(1).asIntegerValue().asInt());
            boolean isShared = av.get(2).asBooleanValue().getBoolean();
            boolean isList = av.get(3).asBooleanValue().getBoolean();

            item = new RTDBItem(data, timestamp, isShared, isList);
        }
        env.close();

        return item;
    }
}
