package nl.robb.jrtdb.comm;

import java.util.Arrays;

/**
 *
 * @author rob
 */
public class RTDBContext {

    private final String ifName;
    private final byte[] ipv4Address;
    private final int port;

    private RTDBContext(String ifName, byte[] ipv4Address, int port) {
        this.ifName = ifName;
        this.ipv4Address = ipv4Address;
        this.port = port;
    }

    /**
     * @return the ifName
     */
    public String getIfName() {
        return ifName;
    }

    /**
     * @return the ipv4Address
     */
    public byte[] getIpv4Address() {
        return ipv4Address;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("interface=%s ip4address=%d.%d.%d.%d port=%d",
                ifName, ipv4Address[0] & 0xff, ipv4Address[1] & 0xff, ipv4Address[2] & 0xff, ipv4Address[3] & 0xff, port);
    }

    public static class Builder {

        private String ifName = "lo";
        private final byte[] ipv4Address = new byte[] { (byte)239, (byte)255, (byte)1, (byte)1 };
        private int port = 21012;

        public Builder() {
        }

        public Builder withInterface(String ifName) {
            this.ifName = ifName;
            return this;
        }

        public Builder withIPv4Address(byte[] ipv4Address) {
            if (ipv4Address == null || ipv4Address.length != 4) {
                throw new IllegalArgumentException("Invalid address: " + Arrays.toString(ipv4Address));
            }
            System.arraycopy(ipv4Address, 0, this.ipv4Address, 0, 4);
            return this;
        }

        public Builder withIPv4Address(String ipv4Address) {
            if (ipv4Address == null) {
                throw new IllegalArgumentException("Invalid address: " + ipv4Address);
            }
            String[] s = ipv4Address.split("\\.");
            if (s.length != 4) {
                throw new IllegalArgumentException("Invalid address: " + ipv4Address);
            }
            for (int i = 0; i < 4; i++) {
                this.ipv4Address[i] = (byte)Integer.parseInt(s[i]);
            }
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public RTDBContext build() {
            return new RTDBContext(ifName, ipv4Address, port);
        }
    }
}
