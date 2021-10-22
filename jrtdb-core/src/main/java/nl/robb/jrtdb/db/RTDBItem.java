package nl.robb.jrtdb.db;

import nl.robb.jrtdb.common.RTDBTimestamp;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author rob
 */
class RTDBItem {

    private final byte[] data;
    private final RTDBTimestamp timestamp;
    private final boolean isShared;
    private final boolean isList;

    RTDBItem(
            @JsonProperty("data") byte[] data,
            @JsonProperty("timestamp") RTDBTimestamp timestamp,
            @JsonProperty("isShared") boolean isShared,
            @JsonProperty("isList") boolean isList) {
        this.data = data;
        this.timestamp = timestamp;
        this.isShared = isShared;
        this.isList = isList;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return the timestamp
     */
    public RTDBTimestamp getTimestamp() {
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
        return String.format("data=%s; timestamp=%.6f; shared=%b; list=%b",
                data, timestamp.getTimestamp(), isShared, isList);
    }
}
