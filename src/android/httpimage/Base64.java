package android.httpimage;

import java.util.Arrays;

/**
 * Base64 encoder
 */
public class Base64 {
    static final byte[] encode(byte abyte0[]) {
        byte abyte1[] = new byte[abyte0.length];
        System.arraycopy(abyte0, 0, abyte1, 0, abyte0.length);
        if (abyte1.length == 0) {
            return abyte1;
        }
        byte abyte2[] = new byte[((abyte1.length - 1) / 3 + 1) * 4];
        int k1 = 0;
        int l1;
        for (l1 = 0; l1 + 3 <= abyte1.length;) {
            int i = (abyte1[l1++] & 0xff) << 16;
            i |= (abyte1[l1++] & 0xff) << 8;
            i |= (abyte1[l1++] & 0xff) << 0;
            int l = (i & 0xfc0000) >> 18;
            abyte2[k1++] = base64[l];
            l = (i & 0x3f000) >> 12;
            abyte2[k1++] = base64[l];
            l = (i & 0xfc0) >> 6;
            abyte2[k1++] = base64[l];
            l = i & 0x3f;
            abyte2[k1++] = base64[l];
        }

        if (abyte1.length - l1 == 2) {
            int j = (abyte1[l1] & 0xff) << 16;
            j |= (abyte1[l1 + 1] & 0xff) << 8;
            int i1 = (j & 0xfc0000) >> 18;
            abyte2[k1++] = base64[i1];
            i1 = (j & 0x3f000) >> 12;
            abyte2[k1++] = base64[i1];
            i1 = (j & 0xfc0) >> 6;
            abyte2[k1++] = base64[i1];
            abyte2[k1++] = 61;
        } else if (abyte1.length - l1 == 1) {
            int k = (abyte1[l1] & 0xff) << 16;
            int j1 = (k & 0xfc0000) >> 18;
            abyte2[k1++] = base64[j1];
            j1 = (k & 0x3f000) >> 12;
            abyte2[k1++] = base64[j1];
            abyte2[k1++] = 61;
            abyte2[k1++] = 61;
        }
        return abyte2;
    }

    static final String encode(String s) {
        return new String(encode(s.getBytes()));
    }

    static final byte[] decode(byte abyte0[]) {
        byte abyte1[] = new byte[abyte0.length];
        System.arraycopy(abyte0, 0, abyte1, 0, abyte0.length);
        if (abyte1.length == 0) {
            return abyte1;
        }
        int i;
        for (i = abyte1.length; abyte1[i - 1] == 61; i--) {
            ;
        }
        byte abyte2[] = new byte[i - abyte1.length / 4];
        for (int j = 0; j < abyte1.length; j++) {
            abyte1[j] = ascii[abyte1[j]];
            if (abyte1[j] == -1 && j < i) {
                throw new IllegalArgumentException((new StringBuilder()).append("Invalid character in base 64 encoding at index ").append(j).toString());
            }
        }

        try {
            int k = 0;
            int l;
            for (l = 0; l < abyte2.length - 2; l += 3) {
                abyte2[l] = (byte) (abyte1[k] << 2 & 0xff | abyte1[k + 1] >>> 4 & 3);
                abyte2[l + 1] = (byte) (abyte1[k + 1] << 4 & 0xff | abyte1[k + 2] >>> 2 & 0xf);
                abyte2[l + 2] = (byte) (abyte1[k + 2] << 6 & 0xff | abyte1[k + 3] & 0x3f);
                k += 4;
            }

            if (l < abyte2.length) {
                abyte2[l] = (byte) (abyte1[k] << 2 & 0xff | abyte1[k + 1] >>> 4 & 3);
            }
            if (++l < abyte2.length) {
                abyte2[l] = (byte) (abyte1[k + 1] << 4 & 0xff | abyte1[k + 2] >>> 2 & 0xf);
            }
        } catch (ArrayIndexOutOfBoundsException arrayindexoutofboundsexception) {
            throw new IllegalArgumentException("Invalid base 64 encoded input");
        }
        return abyte2;
    }

    public static final String decode(String s) {
        return new String(decode(s.getBytes()));
    }

    static final byte[] decodeString(String s) {
        return decode(s.getBytes());
    }

    public static final String encodeBytes(byte abyte0[]) {
        return new String(encode(abyte0));
    }

    private static byte base64[] = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
            85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113,
            114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
    static byte ascii[];

    static {
        ascii = new byte[255];
        Arrays.fill(ascii, (byte) -1);
        for (int i = 0; i < base64.length; i++) {
            ascii[base64[i]] = (byte) i;
        }

    }
}
