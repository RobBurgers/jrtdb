package nl.robb.jrtdb.common;

/**
 *
 * @author rob
 * @param <T>
 */
public class RTDBData<T> {

    private final T data;
    private final RTDBStatus status;
    private final RTDBTimestamp ts;

    public RTDBData(RTDBStatus status) {
        this(status, null, RTDBTimestamp.now());
    }

    public RTDBData(T data, RTDBTimestamp ts) {
        this(RTDBStatus.SUCCESS, data, ts);
    }

    public RTDBData(RTDBStatus status, T data, RTDBTimestamp ts) {
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
        return RTDBTimestamp.millisBetween(RTDBTimestamp.now(), ts);
    }

    /**
     *
     * @return
     */
    public RTDBStatus getStatus() {
        return status;
    }

}
