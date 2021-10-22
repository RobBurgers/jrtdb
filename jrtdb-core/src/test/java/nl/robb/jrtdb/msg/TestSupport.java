package nl.robb.jrtdb.msg;

/**
 *
 * @author rob
 */
public final class TestSupport {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    public static String bytesToHex(byte[] bytes, int offset, int length) {
        char[] hexChars = new char[length * 3];
        for (int j = offset; j < offset + length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = (j + 1) % 8 == 0 ? '\n' : ' ';
        }
        return new String(hexChars);
    }
}
