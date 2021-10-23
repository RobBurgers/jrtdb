package nl.robb.jrtdb.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 *
 * @author rob
 */
public class RTDBTimestamp {

    private final int tvSec;
    private final int tvUsec;

    public static RTDBTimestamp from(double ts) {
        return new RTDBTimestamp((int)ts, (int)((ts - ((int)ts)) * 1e6));
    }

    public static RTDBTimestamp from(Instant ts) {
        return new RTDBTimestamp((int)ts.getEpochSecond(), (int)(ts.getNano()/1000));
    }

    public static RTDBTimestamp now() {
        return from(Instant.now());
    }

    public RTDBTimestamp(
            @JsonProperty("tvSec") int tv_sec,
            @JsonProperty("tvUsec") int tv_usec) {
        if (tv_sec < 0 || tv_usec < 0 || tv_usec > 1000000) {
            throw new IllegalArgumentException(
                    String.format("tv_sec=%d tv_used=%d", tv_sec, tv_usec));
        }
        this.tvSec = tv_sec;
        this.tvUsec = tv_usec;
    }

    @JsonIgnore
    public double getTimestamp() {
        return tvSec + tvUsec * 1e-6;
    }

    public int getTvSec() {
        return tvSec;
    }

    public int getTvUSec() {
        return tvUsec;
    }

    @Override
    public String toString() {
        return String.format("%.6f", getTimestamp());
    }
}
