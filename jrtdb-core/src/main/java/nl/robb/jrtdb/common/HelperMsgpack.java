package nl.robb.jrtdb.common;

import java.io.PrintStream;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;

/**
 *
 * @author rob
 */
public final class HelperMsgpack {

    public static void checkValueTypes(ArrayValue av, ValueType... valueTypes) throws InvalidDataException {
        int i = 0;
        for (ValueType vt : valueTypes) {
            checkValueType(av.get(i++), vt);
        }
    }

    public static void checkValueType(Value v, ValueType valueType, int size) throws InvalidDataException {
        checkValueType(v, valueType);
        switch (valueType) {
            case ARRAY:
                if (v.asArrayValue().size() != size) {
                    throw new InvalidDataException(String.format("Excepted array of size %d but was %d",
                        size, v.asArrayValue().size()));
                }
                return;
            case MAP:
                if (v.asMapValue().size() != size) {
                    throw new InvalidDataException(String.format("Excepted map of size %d but was %d",
                        size, v.asArrayValue().size()));
                }
                return;
        }
        throw new InvalidDataException(String.format("Cannot check size of %s", v.getValueType()));
    }

    public static void checkValueType(Value v, ValueType valueType) throws InvalidDataException {
        if (v.getValueType() != valueType) {
            throw new InvalidDataException(String.format("Excepted value type %s but was %s", valueType.toString(), v.getValueType().toString()));
        }
    }
    
    public static void printValue(Value v) {
        printValue(System.out, v);
    }
    
    public static void printValue(PrintStream ps, Value v) {
        printValue("", ps, v);
    }
    
    private static void printValue(String prefix, PrintStream ps, Value v) {
        if (v.isArrayValue()) {
            ps.println(prefix + "[");
            v.asArrayValue().forEach(vv -> { 
                printValue(prefix + " ", ps, vv);
            });
            ps.println(prefix + "]");
        }
        else
        {
            ps.printf("%s%-7s = %s%n", prefix, v.getValueType(), v.toString());
        }
    }
    
    
    private HelperMsgpack() {
        
    }
}
