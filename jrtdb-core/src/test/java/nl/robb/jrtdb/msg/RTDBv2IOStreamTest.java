package nl.robb.jrtdb.msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import nl.robb.jrtdb.common.InvalidDataException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import nl.robb.jrtdb.comm.RTDBContext;
import nl.robb.jrtdb.comm.RTDBMessageReceiver;
import nl.robb.jrtdb.comm.RTDBMessageSender;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rob
 */
public class RTDBv2IOStreamTest {

    @Test
    public void testSendReceiveData() throws InvalidDataException, IOException, InterruptedException {
        RTDBv2DSFile ds = new RTDBv2DSFile("src/test/resources/rtdb-00.bin");
        Collection<RTDBv2DTO> expected = ds.getAll();
        // confirm that the input is not empty
        Assert.assertTrue(expected.size() > 0);

        // Set up receiver and sender
        final CountDownLatch running = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        RTDBContext ctx = new RTDBContext.Builder()
                .withIPv4Address(new byte[] { (byte)224,(byte)16,(byte)32,(byte)201 })
                .withInterface("lo")
                .withPort(6754)
                .build();
        RTDBMessageReceiver receiver = new RTDBMessageReceiver(ctx, (Collection<RTDBv2DTO> actual) -> {
            Assert.assertEquals(expected.toString(), actual.toString());
            // Countdown does not triggered when assert fails. Timeout occurs.
            done.countDown();
        }, running);
        RTDBMessageSender sender = new RTDBMessageSender(1, ctx, ds);

        Thread t = new Thread(receiver);
        t.start();

        // Wait for receiver to start
        if (!running.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Receiver took too long to start");
        }

        // Now it is time to send the data accross
        sender.send();

        // Wait for reception of data
        if (!done.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Receiver timeout out. No message received.");
        }

        sender.stop();
        receiver.stop();

        t.join(5000);
        Assert.assertFalse("Receiver thread failed to die", t.isAlive());
    }

    @Test
    public void testOutputInput() throws InvalidDataException, IOException {
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
