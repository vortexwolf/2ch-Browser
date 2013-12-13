package com.vortexwolf.dvach.common.utils;

public class XmlUtils {
    public static final int convertValueToInt(CharSequence charSeq, int defaultValue) {
        if (null == charSeq) {
            return defaultValue;
        }

        String nm = charSeq.toString();

        // XXX This code is copied from Integer.decode() so we don't
        // have to instantiate an Integer!

        int value;
        int sign = 1;
        int index = 0;
        int len = nm.length();
        int base = 10;

        if ('-' == nm.charAt(0)) {
            sign = -1;
            index++;
        }

        if ('0' == nm.charAt(index)) {
            // Quick check for a zero by itself
            if (index == (len - 1)) {
                return 0;
            }

            char c = nm.charAt(index + 1);

            if ('x' == c || 'X' == c) {
                index += 2;
                base = 16;
            } else {
                index++;
                base = 8;
            }
        } else if ('#' == nm.charAt(index)) {
            index++;
            base = 16;
        }

        return Integer.parseInt(nm.substring(index), base) * sign;
    }
}
