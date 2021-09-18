package nl.robb.jrtdb.comm;

import java.util.Collection;
import nl.robb.jrtdb.msg.RTDBv2DTO;

/**
 *
 * @author rob
 */
public interface RTDBMessageHandler {
    
    public void process(Collection<RTDBv2DTO> message);
}
