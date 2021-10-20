package nl.robb.jrtdb.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import nl.robb.jrtdb.RTDB;
import nl.robb.jrtdb.common.RTDBData;
import nl.robb.jrtdb.common.RTDBStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rob
 */
public class RTDBStorageTest {

    private static final Logger LOG = Logger.getLogger(RTDBStorageTest.class.getName());

    @Test
    public void test() throws JsonProcessingException {
        ArrayList<DataItem> items = new ArrayList<>();

        items.add(new DataItem("John", 43));
        items.add(new DataItem("Madeline", 35));

        try (RTDBStorage storage = new RTDBStorage(1)) {
            RTDB rtdb = new RTDB(storage);

            // Store items
            RTDBStatus status = rtdb.put("MYITEMS", items.toArray(new DataItem[0]));
            Assert.assertEquals(RTDBStatus.SUCCESS, status);

            // Retrieve items
            RTDBData<DataItem[]> result = rtdb.get("MYITEMS", DataItem[].class);
            Assert.assertTrue(result.isValid());
            Assert.assertEquals(items.size(), result.get().length);

            for (int i = 0; i < 2; i++) {
                Assert.assertEquals(items.get(i).getName(), result.get()[i].getName());
                Assert.assertEquals(items.get(i).getAge(), result.get()[i].getAge());
            }
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
}
