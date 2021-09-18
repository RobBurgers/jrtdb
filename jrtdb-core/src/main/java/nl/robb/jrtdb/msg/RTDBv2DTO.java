package nl.robb.jrtdb.msg;

/**
 *
 * @author rob
 */
public class RTDBv2DTO {

    private final int agent;
    private final String key;
    private final byte[] data;
    private final RTDBv2Timestamp timestamp;
    private final boolean isShared;
    private final boolean isList;

    /**
     * @return the agent
     */
    public int getAgent() {
        return agent;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Time in seconds since 01-01-1970 with microsecond resolution.
     * @return the timestamp
     */
    public RTDBv2Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * @return the isShared
     */
    public boolean isShared() {
        return isShared;
    }

    /**
     * @return the isList
     */
    public boolean isList() {
        return isList;
    }
    
    @Override
    public String toString() {
        return String.format("agent=%d; key=%s; data.length=%d; timestamp=%s; shared=%b; list=%b",
                agent, key, data.length, timestamp, isShared, isList);
    }
    
    private RTDBv2DTO(int agent, String key, byte[] data, RTDBv2Timestamp timestamp, boolean isShared, boolean isList) {
        this.agent = agent;
        this.key = key;
        this.data = data;
        this.timestamp = timestamp;
        this.isShared = isShared;
        this.isList = isList;
    }

    public static class RtDBv2DTOBuilder {
        
        private final int agent;
        private final String key;
        private final byte[] data;
        private RTDBv2Timestamp timestamp;
        private boolean isShared;
        private boolean isList;

        public RtDBv2DTOBuilder(int agent, String key, byte[] data) {
            this.agent = agent;
            this.key = key;
            this.data = data;
            this.timestamp = new RTDBv2Timestamp(0, 0);
        }

        public RtDBv2DTOBuilder withTimestamp(int tv_sec, int tv_usec) {
            timestamp = new RTDBv2Timestamp(tv_sec, tv_usec);
            return this;
        }
        
        public RtDBv2DTOBuilder isShared() {
            this.isShared = true;
            return this;
        }
        
        public RtDBv2DTOBuilder isList() {
            this.isList = true;
            return this;
        }

        RTDBv2DTO build() {
            return new RTDBv2DTO(agent, key, data,
                timestamp, isShared, isList);
        }
    }
}
