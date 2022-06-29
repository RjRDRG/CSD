package com.csd.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Conversion {

    public static BigInteger doubleToBigInteger(double d) {
        return BigDecimal.valueOf(d).toBigInteger();
    }

    public static double bigIntegerToDouble(BigInteger bi) {
        return bi.doubleValue();
    }
}
