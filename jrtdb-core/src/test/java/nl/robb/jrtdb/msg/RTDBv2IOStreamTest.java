package nl.robb.jrtdb.msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import nl.robb.jrtdb.common.InvalidDataException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author rob
 */
public class RTDBv2IOStreamTest {

    @Test
    @Ignore("zstd compression format slightly differs")
    public void testInputOutput() throws InvalidDataException, IOException {
        byte[] dataIn = readFile("src/test/resources/rtdb-00.bin");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        RTDBv2OutputStream ros = new RTDBv2OutputStream(1, bos);

        RTDBv2InputStream ris = new RTDBv2InputStream(new ByteArrayInputStream(dataIn));

        byte[] buffer = new byte[4096];
        int count = ris.read(buffer);
        ros.write(buffer, 0, count);
        ros.flush();

        byte[] dataOut = bos.toByteArray();
        Assert.assertArrayEquals(TestSupport.bytesToHex(dataOut), dataIn, dataOut);
    }

    @Test
    public void testSendData() throws InvalidDataException, IOException {
        byte[] dataIn = readFile("src/test/resources/rtdb-00.bin");
        byte [] IP = { 127, 0, 0, 1 };
        InetAddress address = InetAddress.getByAddress(IP);
        DatagramPacket packet = new DatagramPacket(dataIn, dataIn.length, address, 8001);
        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.send(packet);
    }

    @Test
    public void testSendReceiveData() throws InvalidDataException, IOException {
        // read data from file
        byte[] dataIn = readFile("src/test/resources/rtdb-00.bin");
        RTDBv2InputStream ris = new RTDBv2InputStream(new ByteArrayInputStream(dataIn));
        final byte[] expected = new byte[4096];
        int expectedSize = ris.read(expected);
        Collection<RTDBv2DTO> items1 = new RTDBv2Msgpack().unpack(expected, 0, expectedSize);
        Assert.assertEquals(17, items1.size());

        // rtdb os -> byte array os -> byte array is -> rtdb is
        // write data to output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        RTDBv2OutputStream rtdbos = new RTDBv2OutputStream(1, bos);
        rtdbos.write(expected, 0, expectedSize);
        rtdbos.flush();

        // read data from input stream
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        RTDBv2InputStream rtdbis = new RTDBv2InputStream(bis);

        // read data from is
        byte[] actual = new byte[4096];
        int actualSize = rtdbis.read(actual);

        Assert.assertEquals(expectedSize, actualSize);
        Assert.assertArrayEquals(expected, actual);

        Collection<RTDBv2DTO> items2 = new RTDBv2Msgpack().unpack(actual, 0, actualSize);
        Assert.assertEquals(items1.size(), items2.size());
        Assert.assertEquals(items1.toString(), items2.toString());
    }

    private byte[] readFile(String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            byte[] buffer = new byte[4096];
            int count = fis.read(buffer);
            byte[] result = new byte[count];
            System.arraycopy(buffer, 0, result, 0, count);
            return result;
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }
        return new byte[0];
    }
}
