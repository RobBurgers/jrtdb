package nl.robb.jrtdb.msg;

import nl.robb.jrtdb.common.InvalidDataException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rob
 */
public class RTDBv2MsgpackTest {
    
    @Test
    public void testParseData() throws InvalidDataException, IOException {
        Collection<RTDBv2DTO> result = parseFile("src/test/resources/rtdb-01.bin");
        Assert.assertEquals(17, result.size());
    }
    
    @Test
    public void testParseDataAll() throws InvalidDataException, IOException {
        for (int i = 0; i < 10; i++) {
            String fileName = String.format("src/test/resources/rtdb-%02d.bin", i);
            Collection<RTDBv2DTO> result = parseFile(fileName);
            System.out.printf("%s: %s%n", fileName, result);
        }
    }
    
    @Test
    public void testUnpackPack() throws InvalidDataException, IOException {
        byte[] input = readFile("src/test/resources/rtdb-09.bin");
        RTDBv2Msgpack mp = new RTDBv2Msgpack();
        byte[] output = mp.pack(mp.unpack(input));
        
        Assert.assertArrayEquals(input, output);
    }
    
    private Collection<RTDBv2DTO> parseFile(String fileName) {
        try {
            return new RTDBv2Msgpack().unpack(readFile(fileName));
        }
        catch (IOException | InvalidDataException ioe) {
            Assert.fail(ioe.getMessage());
        }
        return new ArrayList<>();
    }
    
    private byte[] readFile(String fileName) {
        try (RTDBv2InputStream ris = new RTDBv2InputStream(new FileInputStream(fileName))) {
            byte[] buffer = new byte[4096];
            int count = ris.read(buffer);
            byte[] result = new byte[count];
            System.arraycopy(buffer, 0, result, 0, count);
            return result;
        }
        catch (Exception ioe) {
            Assert.fail(ioe.getMessage());
        }
        return new byte[0];
    }
}
