package nl.robb.jrtdb.msg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import nl.robb.jrtdb.common.InvalidDataException;
import nl.robb.jrtdb.common.RTDBDataSource;

/**
 * Data store that support file storage.
 * @author rob
 */
public class RTDBv2DSFile implements RTDBDataSource<RTDBv2DTO> {

    private final File file;

    public RTDBv2DSFile(File file) {
        this.file = file;
    }

    public RTDBv2DSFile(String fileName) {
        this.file = new File(fileName);
    }

    @Override
    public boolean isRead() {
        return true;
    }

    @Override
    public Collection<RTDBv2DTO> get(String key) throws IOException {
        return getAll();
    }

    @Override
    public Collection<RTDBv2DTO> getAll() throws IOException {
        try {
            return new RTDBv2Msgpack().unpack(readFile(file));
        }
        catch (InvalidDataException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }

    private byte[] readFile(File file) throws IOException {
        try (RTDBv2InputStream ris = new RTDBv2InputStream(new FileInputStream(file))) {
            return ris.readAllBytes();
        }
    }
}
