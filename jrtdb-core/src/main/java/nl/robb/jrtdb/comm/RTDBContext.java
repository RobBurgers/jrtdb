package nl.robb.jrtdb.comm;

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
