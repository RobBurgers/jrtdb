package nl.robb.jrtdb.comm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.robb.jrtdb.common.RTDBDataSource;
import nl.robb.jrtdb.msg.RTDBv2DTO;
import nl.robb.jrtdb.msg.RTDBv2Msgpack;
import nl.robb.jrtdb.msg.RTDBv2OutputStream;

/**
 *
 * @author rob
 */
public class RTDBMessageSender {

    private static final Logger LOG = Logger.getLogger(RTDBMessageSender.class.getName());

    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private final RTDBv2Msgpack packer = new RTDBv2Msgpack();
    private final RTDBContext ctx;
    private final RTDBDataSource<RTDBv2DTO> ds;
    private final RTDBv2OutputStream rtdbos;

    private MulticastSocket socket;
    private InetAddress address;
    private volatile boolean isStopped = false;

    public RTDBMessageSender(int agentId, RTDBContext ctx, RTDBDataSource<RTDBv2DTO> ds) {
        this.ctx = ctx;
        this.ds = ds;
        this.rtdbos = new RTDBv2OutputStream(agentId, bos);
        init();
    }

    private void init() {
        try {
            address = InetAddress.getByAddress(ctx.getIpv4Address());
            NetworkInterface ni = NetworkInterface.getByName(ctx.getIfName());
            socket = new MulticastSocket();
            socket.setNetworkInterface(ni);
        }
        catch (IOException ioe) {
            LOG.log(Level.SEVERE, ioe.getMessage(), ioe);
        }
    }

    public void send() {
        if (isStopped) {
            LOG.log(Level.WARNING, "Cannot send after stop.");
            return;
        }
        try {
            bos.reset();
            rtdbos.write(packer.pack(ds.getAll()));
            rtdbos.flush();
            socket.send(new DatagramPacket(bos.toByteArray(), bos.size(), address, ctx.getPort()));
        }
        catch (IOException ioe) {
            if (!isStopped) {
                LOG.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }
    }

    public void stop() {
        isStopped = true;
        if (socket != null) {
            socket.close();
        }
    }
}
