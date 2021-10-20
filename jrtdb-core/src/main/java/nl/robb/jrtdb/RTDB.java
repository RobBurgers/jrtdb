package nl.robb.jrtdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.robb.jrtdb.common.RTDBData;
import nl.robb.jrtdb.common.RTDBDataSource;
import nl.robb.jrtdb.common.RTDBStatus;
import nl.robb.jrtdb.msg.RTDBv2DTO;
import org.msgpack.jackson.dataformat.JsonArrayFormat;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 *
 * @author rob
 */
public class RTDB {

    private final static Logger LOG = Logger.getLogger(RTDB.class.getName());

    private final RTDBDataSource<RTDBv2DTO> source;
    private final ObjectMapper objectMapper;

    public RTDB(RTDBDataSource<RTDBv2DTO> source) {
        this.source = source;
        this.objectMapper = new ObjectMapper(new MessagePackFactory());
        objectMapper.setAnnotationIntrospector(new JsonArrayFormat());
    }

    public <T> T getValue(String key, Class<T> clazz) {
        try {
            return source.get(key).stream()
                    .filter(e -> { return key.equals(e.getKey()); })
                    .findFirst()
                    .map((RTDBv2DTO v) -> {
                        try {
                            return objectMapper.readValue(v.getData(), clazz);
                        } catch (IOException ex) {
                            LOG.log(Level.SEVERE, "Failed to read " + key, ex);
                            return null;
                        }
                    })
                    .orElse(null);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to retrieve " + key, ex);
            return null;
        }
    }

    public <T> RTDBData<T> get(String key, Class<T> clazz) {
        try {
            return source.get(key).stream()
                    .filter(e -> { return key.equals(e.getKey()); })
                    .findFirst()
                    .map((RTDBv2DTO v) -> {
                        try {
                            T t = objectMapper.readValue(v.getData(), clazz);
                            return new RTDBData<T>(t, v.getInstant());
                        } catch (IOException ex) {
                            LOG.log(Level.SEVERE, "Failed to read " + key, ex);
                            return new RTDBData<T>(RTDBStatus.FAILED_DECOMPRESSING);
                        }
                    })
                    .orElse(new RTDBData<>(RTDBStatus.KEY_NOT_FOUND));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to retrieve " + key, ex);
            return new RTDBData<>(RTDBStatus.STORAGE_DOES_NOT_EXISTS);
        }
    }

    public <T> RTDBStatus put(String key, T t) {
        try {
            byte[] serialized = objectMapper.writeValueAsBytes(t);
            RTDBv2DTO dto = new RTDBv2DTO.RtDBv2DTOBuilder(1, key, serialized) // TODO: Agent id
                    .build();
            source.put(key, dto);
            return RTDBStatus.SUCCESS;
        } catch (JsonProcessingException ex) {
            LOG.log(Level.SEVERE, "Failed to store " + key, ex);
            return RTDBStatus.FAILED_COMPRESSING;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to store " + key, ex);
            return RTDBStatus.INTERNAL_MDB_ERROR;
        }
    }
}
