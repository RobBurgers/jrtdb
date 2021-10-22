package nl.robb.jrtdb.msg;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.robb.jrtdb.RTDB;
import nl.robb.jrtdb.common.RTDBData;
import nl.robb.jrtdb.common.RTDBDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.msgpack.jackson.dataformat.JsonArrayFormat;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 *
 * @author rob
 */
public class RTDBTest {

    private static final Logger LOG = Logger.getLogger(RTDBTest.class.getName());

    private class TestDataSource implements RTDBDataSource<RTDBv2DTO> {

        private final HashMap<String, RTDBv2DTO> store = new HashMap<>();

        @Override
        public Collection<RTDBv2DTO> get(String key) {
            ArrayList<RTDBv2DTO> result = new ArrayList<>();
            if (store.containsKey(key)) {
                result.add(store.get(key));
            }
            return result;
        }

        @Override
        public void put(String key, RTDBv2DTO data) {
            store.put(key, data);
        }

        @Override
        public boolean isRead() {
            return true;
        }

        @Override
        public boolean isWrite() {
            return true;
        }

        @Override
        public void close() {
            store.clear();
        }
    }

    public static class DataItem {
        private final String name;
        private final int age;

        public DataItem(
                @JsonProperty("name") String name,
                @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return name + ":" + age;
        }
    }

    @Test
    public void test() throws JsonProcessingException {
        ArrayList<DataItem> items = new ArrayList<>();

        items.add(new DataItem("John", 43));
        items.add(new DataItem("Madeline", 35));

        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        objectMapper.setAnnotationIntrospector(new JsonArrayFormat());

        byte[] serialized = objectMapper.writeValueAsBytes(items);
        RTDBv2DTO dto = new RTDBv2DTO.RtDBv2DTOBuilder(1, "ITEMS", serialized)
                .build();

        TestDataSource ds = new TestDataSource();
        ds.put("ITEMS", dto);

        RTDB rtdb = new RTDB(ds);
        DataItem[] result = getValue(rtdb, "ITEMS", DataItem[].class);
        Assert.assertEquals(2, result.length);
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(items.get(i).getName(), result[i].getName());
            Assert.assertEquals(items.get(i).getAge(), result[i].getAge());
        }
    }

    private <T> T getValue(RTDB rtdb, String key, Class<T> clazz) {
        RTDBData<T> t = rtdb.get(key, clazz);
        if (!t.isValid()) {
            LOG.log(Level.WARNING, "Failed to retrieve data: key={0} status={1}",
                    new Object[] {key, t.getStatus()});
        }
        return t.get();
    }
}
