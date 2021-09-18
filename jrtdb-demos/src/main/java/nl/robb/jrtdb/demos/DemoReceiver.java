package nl.robb.jrtdb.demos;

import nl.robb.jrtdb.comm.RTDBContext;
import nl.robb.jrtdb.comm.RTDBMessageReceiver;

/**
 *
 * @author rob
 */
public class DemoReceiver {

    public static void main(String[] args) throws InterruptedException {
        RTDBContext ctx = new RTDBContext.Builder()
                .withInterface("lo")
                .withIPv4Address("224.16.32.56")
                .withPort(8011)
                .build();
        RTDBMessageReceiver r = new RTDBMessageReceiver(ctx, m -> { System.out.println(m); });
        new Thread(r).start();
        Thread.currentThread().join();
    }
}
