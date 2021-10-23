package nl.robb.jrtdb.msg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.luben.zstd.Zstd;
import java.io.IOException;
import java.io.OutputStream;
import nl.robb.jrtdb.common.RTDBTimestamp;
import org.msgpack.jackson.dataformat.JsonArrayFormat;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * Input: Raw RtDB message
 * Output: Decompressed RtDB data
 * @author rob
 */
public class RTDBv2OutputStream extends OutputStream {

    private static final int COMPRESSION_LEVEL = 1;

    private final int agentId;
    private final OutputStream os;
    private final ObjectMapper mapper;
    private final byte[] data = new byte[4096];
    private int idx = 0;
    private int counter = 0;

    public RTDBv2OutputStream(int agentId, OutputStream os) {
        this.agentId = agentId;
        this.os = os;
        this.mapper = new ObjectMapper(new MessagePackFactory());
        mapper.setAnnotationIntrospector(new JsonArrayFormat());
    }

    @Override
    public void write(int i) throws IOException {
        if (idx < data.length) {
            data[idx++] = (byte) i;
        }
        else {
            throw new IOException("Buffer size exceeded");
        }
    }

    @Override
    public void flush() throws IOException {
        writeMessage();
    }

    private void writeMessage() throws IOException {
        byte[] message = new byte[4096];
        // write header to message
        RTDBv2Header header = new RTDBv2Header(
                agentId, counter++, RTDBTimestamp.now());
        byte[] packedHeader = mapper.writeValueAsBytes(header);

        message[0] = (byte)packedHeader.length;
        System.arraycopy(packedHeader, 0, message, 1, packedHeader.length);
        // append compressed data to message
        // (dst, start, size remaining, src, start, length, level)
        int total = 1 + packedHeader.length;
        int count = (int) Zstd.compressByteArray(
                message, total, message.length - total,
                data, 0, idx, COMPRESSION_LEVEL);
        total += count;

        os.write(message, 0, total);
        os.flush();
        idx = 0;
    }
}
