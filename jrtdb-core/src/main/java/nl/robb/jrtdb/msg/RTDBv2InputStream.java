package nl.robb.jrtdb.msg;

import com.github.luben.zstd.Zstd;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input: Raw RtDB message
 * Output: Decompressed RtDB data
 * @author rob
 */
public class RTDBv2InputStream extends InputStream {
    
    private final InputStream is;
    private final byte[] data = new byte[4096];
    private int remaining = 0;
    private int total = 0;

    public RTDBv2InputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        int result = -1;
        if (remaining == 0) {
            readMessage();
        }
        if (remaining > 0 ) {
            remaining--;
            byte b = data[total - remaining];
            result = b > 0 ? b : 256 + b;
        }
        return result;
    }
    
    private void readMessage() throws IOException {
        int size = is.read();
        if (size < 0) {
            total = 0;
            remaining = 0;
            return;
        }
        byte[] header = new byte[size];
        is.read(header, 0, header.length);

        // all bytes must be available on first read, otherwise decompression is incomplete
        byte[] compressed = new byte[4096];
        int compressedSize = is.read(compressed);
        total = (int) Zstd.decompressByteArray(data, 0, 4096, compressed, 0, compressedSize);
        total--; // TODO: decompressed data contains one (yet unexplained) byte too many
        
        remaining = total;
    }
}
