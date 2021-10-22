package nl.robb.jrtdb.db;

import nl.robb.jrtdb.common.RTDBTimestamp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import nl.robb.jrtdb.common.RTDBDataSource;
import nl.robb.jrtdb.msg.RTDBv2DTO;

/**
 *
 * @author rob
 */
public class RTDBStorage implements RTDBDataSource<RTDBv2DTO>  {

    private static final String DB_ROOT_PATH = "/tmp/rtdb2_storage";
    private static final String DB_DEFAULT = "default";

    private final RTDBLMDB database;
    private final int remoteAgent;

    public RTDBStorage(int agent) {
        this(DB_DEFAULT, agent);
    }

    public RTDBStorage(String database, int agent) {
        this(DB_ROOT_PATH, database, agent, agent);
    }

    public RTDBStorage(String rootPath, String database, int agent, int remoteAgent) {
        this.database = new RTDBLMDB(new File(
                String.format("%s/%d/%s/agent%d", rootPath, agent, database, remoteAgent)));
        this.remoteAgent = remoteAgent;
    }

    @Override
    public void close() {
        database.close();
    }

    @Override
    public boolean isRead() {
        return true;
    }

    @Override
    public Collection<RTDBv2DTO> get(String key) throws IOException {
        RTDBItem item = database.get(key);

        ArrayList<RTDBv2DTO> result = new ArrayList<>();
        if (item != null) {
            result.add(toDTO(key, item));
        }
        return result;
    }

    @Override
    public Collection<RTDBv2DTO> getAll() throws IOException {
        ArrayList<RTDBv2DTO> result = new ArrayList<>();

        Map<String, RTDBItem> entries = database.getAll();
        for (Map.Entry<String, RTDBItem> entry : entries.entrySet()) {
            if (entry.getValue() != null) {
                result.add(toDTO(entry.getKey(), entry.getValue()));
            }
        }
        return result;
    }

    @Override
    public void put(String key, RTDBv2DTO data) throws IOException {
        database.put(key, toRTDBItem(data));
    }

    private RTDBv2DTO toDTO(String key, RTDBItem item) {
        RTDBv2DTO.RtDBv2DTOBuilder builder = new RTDBv2DTO.RtDBv2DTOBuilder(remoteAgent, key, item.getData())
            .withTimestamp(item.getTimestamp().getTvSec(), item.getTimestamp().getTvUSec());
        if (item.isList()) {
            builder = builder.isList();
        }
        if (item.isShared()) {
            builder = builder.isShared();
        }
        return builder.build();
    }

    private RTDBItem toRTDBItem(RTDBv2DTO data) {
        RTDBTimestamp ts = new RTDBTimestamp(data.getInstant());
        return new RTDBItem(data.getData(), ts, data.isShared(), data.isList());
    }
}
