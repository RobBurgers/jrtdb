package nl.robb.jrtdb.common;

import java.io.IOException;
import java.util.Collection;

/**
 *
 * @author rob
 * @param <T>
 */
public interface RTDBDataSource<T> {

    /**
     * Returns whether this data store supports reading of data
     * @return true when data this data store supports reading, false otherwise
     */
    default boolean isRead() {
        return false;
    }

    /**
     * Returns whether this data store supports writing of data
     * @return true when data this data store supports writing, false otherwise
     */
    default boolean isWrite() {
        return false;
    }

    /**
     * Returns a collection containing any number of elements
     * that may contain elements having the requested key.
     * @param key the key whose associated values are to be returned
     * @return collection of values
     * @throws java.io.IOException when data store is not accessible.
     */
    default Collection<T> get(String key) throws IOException {
        throw new IOException("Cannot read from this data source");
    }

    /**
     * Store data in this data store.
     * @param key key to which the data is to be assiociated
     * @param data data to store
     * @throws java.io.IOException when data store is not accessible.
     */
    default void put(String key, T data) throws IOException {
        throw new IOException("Cannot write to this data source");
    }

    /**
     * Returns a collection containing all elements contained
     * in this data source.
     * @return collections of all values in this data store
     * @throws java.io.IOException when data store is not accessible.
     */
    default Collection<T> getAll() throws IOException {
        throw new IOException("Cannot read from this data source");
    }
}
