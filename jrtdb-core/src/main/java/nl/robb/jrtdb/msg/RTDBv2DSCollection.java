package nl.robb.jrtdb.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import nl.robb.jrtdb.common.RTDBDataSource;

/**
 * Data store that exposes a fixed collection of items.
 * @author rob
 */
public class RTDBv2DSCollection implements RTDBDataSource<RTDBv2DTO> {

    private final Collection<RTDBv2DTO> items = new ArrayList<>();

    public RTDBv2DSCollection(Collection<RTDBv2DTO> items) {
        this.items.addAll(items);
    }

    @Override
    public void close() {
        items.clear();
    }

    @Override
    public boolean isRead() {
        return true;
    }

    @Override
    public Collection<RTDBv2DTO> get(String key) throws IOException {
        return getAll().stream()
                .filter(e -> { return key.equals(e.getKey()); })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<RTDBv2DTO> getAll() throws IOException {
        return items;
    }
}
