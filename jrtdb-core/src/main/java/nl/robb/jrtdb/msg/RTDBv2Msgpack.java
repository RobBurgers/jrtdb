package nl.robb.jrtdb.msg;

import nl.robb.jrtdb.common.InvalidDataException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;
import static nl.robb.jrtdb.common.HelperMsgpack.*;

/**
 *
 * @author rob
 */
public class RTDBv2Msgpack {

    private static final int IDX_KEY = 0;
    private static final int IDX_AGENT = 1;
    private static final int IDX_DATA = 2;
    private static final int IDX_TIMESTAMP = 3;
    private static final int IDX_SHARED = 4;
    private static final int IDX_LIST = 5;

    public byte[] pack(Collection<RTDBv2DTO> collection) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packArrayHeader(1);
        packer.packArrayHeader(collection.size());
        for (RTDBv2DTO d : collection) {
            packer.packArrayHeader(6);
            // key
            packer.packString(d.getKey());
            // agent
            packer.packInt(d.getAgent());

            // data
            packer.packRawStringHeader(d.getData().length);
            packer.writePayload(d.getData());

            // timestamp
            packer.packArrayHeader(2);
            int tvSec = d.getInstant().getTvSec();
            int tvUSec = d.getInstant().getTvUSec();
            packer.packInt(tvSec);
            packer.packInt(tvUSec);

            // isShared
            packer.packBoolean(d.isShared());
            // isList
            packer.packBoolean(d.isList());
        }
        return packer.toByteArray();
    }

    /**
     * Unpacks RTDBv2 msgpacked data to RTDBv2 DTO.
     * @param data msgpacked RTDBv2 data, byte array of uncompressed data
     * @return Collection of RTDBv2 DTOs
     * @throws InvalidDataException when input data does not conform to RTDBv2 structure
     * @throws IOException when extracting data from byte array fails
     */
    public Collection<RTDBv2DTO> unpack(byte[] data) throws InvalidDataException, IOException {
        return unpack(data, 0, data.length);
    }

    public Collection<RTDBv2DTO> unpack(byte[] data, int offset, int length) throws InvalidDataException, IOException {

        List<RTDBv2DTO> result = new ArrayList<>();
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data, offset, length);

        if (!unpacker.hasNext()) {
            return result;
        }
        Value v = unpacker.unpackValue();
        checkValueType(v, ValueType.ARRAY, 1);

        Value w = v.asArrayValue().get(0);
        checkValueType(w, ValueType.ARRAY);

        for (Value e : w.asArrayValue()) {
            result.add(unpackDTO(e));
        }
//        System.out.println("Data available? " + unpacker.hasNext());
//        if (unpacker.hasNext()) {
//            v = unpacker.unpackValue();
//            MessagePackRtDB.printValues("", v);
//        }
        return result;
    }

    private RTDBv2DTO unpackDTO(Value v) throws InvalidDataException {
        checkValueType(v, ValueType.ARRAY, 6);

        ArrayValue av = v.asArrayValue();
        checkValueTypes(av,
                ValueType.STRING, // key
                ValueType.INTEGER, // agent
                ValueType.STRING, // custom data
                ValueType.ARRAY, // timestamp
                ValueType.BOOLEAN, // shared
                ValueType.BOOLEAN // list
        );

        RTDBv2DTO.RtDBv2DTOBuilder builder = new RTDBv2DTO.RtDBv2DTOBuilder(
                av.get(IDX_AGENT).asIntegerValue().asInt(),
                av.get(IDX_KEY).asStringValue().asString(),
                av.get(IDX_DATA).asStringValue().asByteArray());
        if (av.get(IDX_SHARED).asBooleanValue().getBoolean()) {
            builder = builder.isShared();
        }
        if (av.get(IDX_LIST).asBooleanValue().getBoolean()) {
            builder = builder.isList();
        }
        ArrayValue tsav = av.get(IDX_TIMESTAMP).asArrayValue();
        checkValueTypes(tsav, ValueType.INTEGER, ValueType.INTEGER);
        builder = builder.withTimestamp(tsav.get(0).asIntegerValue().asInt(), tsav.get(1).asIntegerValue().asInt());

        return builder.build();
    }
}
