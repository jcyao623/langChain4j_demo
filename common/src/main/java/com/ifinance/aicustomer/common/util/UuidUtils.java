package com.ifinance.aicustomer.common.util;

import java.util.UUID;

/**
 * 通用工具类。
 */
public final class UuidUtils {

    private UuidUtils() {
    }

    /**
     * 生成不带连字符的 UUID。
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
