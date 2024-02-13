package nl.robb.jrtdb.comm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.robb.jrtdb.common.InvalidDataException;
import nl.robb.jrtdb.msg.RTDBv2Msgpack;
import nl.robb.jrtdb.msg.RTDBv2InputStream;

/**
 *
 * @author rob
 */
public class RTDBMessageReceiver implements Runnable {

    private static final Logger LOG = Logger.getLogger(RTDBMessageReceiver.class.getName());
    private static final int MAXPACKETSIZE = 4096;

    private final RTDBContext ctx;
    private final RTDBMessageHandler handler;
    private final CountDownLatch runningSignal;
    private MulticastSocket multicastSocket;
    private volatile boolean isStopped = false;

    public RTDBMessageReceiver(RTDBContext ctx, RTDBMessageHandler handler) {
        this (ctx, handler, new CountDownLatch(1));
    }

    public RTDBMessageReceiver(RTDBContext ctx, RTDBMessageHandler handler, CountDownLatch runningSignal) {
        this.ctx = ctx;
        this.handler = handler;
        this.runningSignal = runningSignal;
    }

    @Override
    public void run() {
        LOG.log(Level.INFO, "Waiting for connection: {0}", ctx);
        try {
            multicastSocket = new MulticastSocket(ctx.getPort());
            InetAddress address = InetAddress.getByAddress(ctx.getIpv4Address());
            SocketAddress sa = new InetSocketAddress(address, ctx.getPort());
            NetworkInterface ni = NetworkInterface.getByName(ctx.getIfName());
            multicastSocket.joinGroup(sa, ni);

            runningSignal.countDown();

            while (!isStopped) {
                // Wait for a packet
                byte[] packetBuf = new byte[MAXPACKETSIZE];
                DatagramPacket packet = new DatagramPacket(packetBuf, packetBuf.length);
                multicastSocket.receive(packet);
                if (handler != null && packet.getLength() > 0) {
                    try {
                        RTDBv2InputStream ris = new RTDBv2InputStream(
                                new ByteArrayInputStream(
                                        packet.getData(), packet.getOffset(), packet.getLength()));
                        byte[] dataBuf = new byte[MAXPACKETSIZE];
                        int count = ris.read(dataBuf);
                        handler.process(new RTDBv2Msgpack().unpack(dataBuf, 0, count));
                    } catch (InvalidDataException ex) {
                        LOG.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
             }
        }
        catch (IOException ioe) {
            if (!isStopped) {
                LOG.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }
    }

    public void stop() {
        isStopped = true;
        if (multicastSocket != null) {
            multicastSocket.close();
        }
    }
}
