package com.dormitory.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**

 * 
 * @author 王和友
 * @since 2026
 */
public class Md5Util {

    /**
     * MD5加密（无盐值）
     * 
     * @param str 待加密的字符串
     * @return String 加密后的32位十六进制字符串
     */
    public static String encrypt(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * MD5加密（带盐值）
     * 
     * @param str 待加密的字符串
     * @param salt 盐值，用于增强加密安全性
     * @return String 加密后的32位十六进制字符串
     */
    public static String encrypt(String str, String salt) {
        return encrypt(str + salt);
    }
}
