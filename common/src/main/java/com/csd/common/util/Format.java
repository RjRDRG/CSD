package com.csd.common.util;

import java.util.Arrays;

public class Format {
    public static String exception(Exception e) {
        return e.getClass().getName() + ": " + e.getMessage() + "\n" +
                Arrays.stream(e.getStackTrace()).map(s -> s.toString() + "\n\t").reduce("", (s0,s1) -> s0+s1);
    }
}
