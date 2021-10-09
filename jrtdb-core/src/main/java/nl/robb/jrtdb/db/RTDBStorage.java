package nl.robb.jrtdb.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import nl.robb.jrtdb.common.RTDBDataSource;
import nl.robb.jrtdb.msg.RTDBv2DTO;

/**
 *
 * @author rob
 */
public class RTDBStorage implements RTDBDataSource<RTDBv2DTO> {

    private static final String DB_ROOT_PATH = "/tmp/rtdb2_storage";
    private static final String DB_DEFAULT = "default";

    private final RTDBLMDB database;

    public RTDBStorage(int agent) {
        this(DB_DEFAULT, agent);
    }

    public RTDBStorage(String database, int agent) {
        this(DB_ROOT_PATH, database, agent, agent);
    }

    public RTDBStorage(String rootPath, String database, int agent, int remoteAgent) {
        this.database = new RTDBLMDB(new File(
                String.format("%s/%d/%s/agent%d", rootPath, agent, database, remoteAgent)));
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
            RTDBv2DTO.RtDBv2DTOBuilder builder = new RTDBv2DTO.RtDBv2DTOBuilder(1, key, item.getData())
                .withTimestamp(item.getTimestamp().getTvSec(), item.getTimestamp().getTvUSec());
            if (item.isList()) {
                builder = builder.isList();
            }
            if (item.isShared()) {
                builder = builder.isShared();
            }
            result.add(builder.build());
        }
        return result;
    }

    @Override
    public Collection<RTDBv2DTO> getAll() throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }
}
