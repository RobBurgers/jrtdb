package nl.robb.jrtdb.common;

/**
 *
 * @author rob
 */
public enum RTDBStatus {
    SUCCESS(0),
    KEY_NOT_FOUND(-1),
    FAILED_PARSING_CONFIG_FILE(-2),
    REMOTE_NOT_FOUND(-3),
    INTEGER_ID_NOT_FOUND(-4),
    VALUE_POINTING_TO_NULL(-5),
    STORAGE_DOES_NOT_EXISTS(-6),
    FAILED_DECOMPRESSING(-7),
    FAILED_COMPRESSING(-8),
    FAILED_DESERIALIZE(-9),
    FAILED_SEMAPHORE_CREATION(-10),
    FAILED_SEMAPHORE_WAIT(-11),
    INTERNAL_MDB_ERROR(-12),
    ITEM_STALE(-13),
    FAILED_SEMAPHORE_RELEASE(-14);

    private int statusCode;

    private RTDBStatus(int statusCode) {
        this.statusCode = statusCode;
    }
}
