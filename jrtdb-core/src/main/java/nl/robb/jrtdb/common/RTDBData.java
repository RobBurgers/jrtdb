package nl.robb.jrtdb.common;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author rob
 * @param <T>
 */
public class RTDBData<T> {

    private final T data;
    private final RTDBStatus status;
    private final Instant ts;

    public RTDBData(RTDBStatus status) {
        this(status, null, Instant.now());
    }

    public RTDBData(T data, Instant ts) {
        this(RTDBStatus.SUCCESS, data, ts);
    }

    public RTDBData(RTDBStatus status, T data, Instant ts) {
        this.status = status;
        this.data = data;
        this.ts = ts;
    }

    public boolean isValid() {
        return status == RTDBStatus.SUCCESS;
    }

    public T get() {
        return data;
    }

    /**
     * Return age in millisecond
     * @return 
     */
    public long getAge() {
        return ChronoUnit.MILLIS.between(ts, Instant.now());
    }

    /**
     * 
     * @return 
     */
    public RTDBStatus getStatus() {
        return status;
    }
}
