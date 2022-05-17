package nl.robb.jrtdb.msg;

import nl.robb.jrtdb.common.HelperMsgpack;
import nl.robb.jrtdb.common.RTDBTimestamp;

/**
 *
 * @author rob
 */
public class RTDBv2DTO {

    private final int agent;
    private final String key;
    private final byte[] data;
    private final RTDBTimestamp timestamp;
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
     * @return the encoded data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return the timestamp
     */
    public RTDBTimestamp getInstant() {
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

    /**
     * @return the decoded data as string
     */
    public String getDataAsString() {
        return HelperMsgpack.toString(HelperMsgpack.unpack(data, 0, data.length));
    }

    @Override
    public String toString() {
        return String.format("agent=%d; key=%s; data=%s; timestamp=%s; shared=%b; list=%b",
                agent, key, getDataAsString(), timestamp, isShared, isList);
    }

    private RTDBv2DTO(int agent, String key, byte[] data, RTDBTimestamp timestamp,
            boolean isShared, boolean isList) {
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
        private RTDBTimestamp timestamp = RTDBTimestamp.now();
        private boolean isShared;
        private boolean isList;

        public RtDBv2DTOBuilder(int agent, String key, byte[] data) {
            this.agent = agent;
            this.key = key;
            this.data = data;
        }

        public RtDBv2DTOBuilder withTimestamp(int tv_sec, int tv_usec) {
            timestamp = new RTDBTimestamp(tv_sec, tv_usec);
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

        public RTDBv2DTO build() {
            return new RTDBv2DTO(agent, key, data,
                timestamp, isShared, isList);
        }
    }
}
