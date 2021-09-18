package nl.robb.jrtdb.msg;

/**
 *
 * @author rob
 */
public class RTDBv2Timestamp {
    
    private final int tv_sec;
    private final int tv_usec;
    
    public RTDBv2Timestamp(double ts) {
        this ((int)ts, (int)((ts - ((int)ts)) * 1e6));
    }
    
    public RTDBv2Timestamp(int tv_sec, int tv_usec) {
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
    
    int getTvSec() {
        return tv_sec;
    }
    
    int getTvUSec() {
        return tv_usec;
    }
    
    @Override
    public String toString() {
        return String.format("%.6f", getTimestamp());
    }
}
