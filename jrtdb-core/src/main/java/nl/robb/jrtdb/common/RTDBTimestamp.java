package nl.robb.jrtdb.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 *
 * @author rob
 */
public class RTDBTimestamp {

    private final int tv_sec;
    private final int tv_usec;

    public RTDBTimestamp(double ts) {
        this ((int)ts, (int)((ts - ((int)ts)) * 1e6));
    }

    public RTDBTimestamp(Instant ts) {
        this ((int)ts.getEpochSecond(), (int)(ts.getNano()/1000));
    }

    public RTDBTimestamp(
            @JsonProperty("tv_sec") int tv_sec,
            @JsonProperty("tv_usec") int tv_usec) {
        if (tv_sec < 0 || tv_usec < 0 || tv_usec > 1000000) {
            throw new IllegalArgumentException(
                    String.format("tv_sec=%d tv_used=%d", tv_sec, tv_usec));
        }
        this.tv_sec = tv_sec;
        this.tv_usec = tv_usec;
    }

    public double getTimestamp() {
        return tv_sec + tv_usec * 1e-6;
    }

    public int getTvSec() {
        return tv_sec;
    }

    public int getTvUSec() {
        return tv_usec;
    }

    @Override
    public String toString() {
        return String.format("%.6f", getTimestamp());
    }
}
