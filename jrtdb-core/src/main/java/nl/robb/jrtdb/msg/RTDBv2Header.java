package nl.robb.jrtdb.msg;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.robb.jrtdb.common.RTDBTimestamp;

/**
 *
 * @author rob
 */
class RTDBv2Header {

    private final int agentId;
    private final int counter;
    private final RTDBTimestamp timestamp;

    RTDBv2Header(
            @JsonProperty("agentId") int agentId,
            @JsonProperty("counter") int counter,
            @JsonProperty("timestamp") RTDBTimestamp timestamp) {
        this.agentId = agentId;
        this.counter = counter;
        this.timestamp = timestamp;
    }

    /**
     * @return the agentId
     */
    public int getAgentId() {
        return agentId;
    }

    /**
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * @return the timestamp
     */
    public RTDBTimestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("agentId=%d; counter=%d; timestamp=%.6f",
                agentId, counter, timestamp.getTimestamp());
    }
}
