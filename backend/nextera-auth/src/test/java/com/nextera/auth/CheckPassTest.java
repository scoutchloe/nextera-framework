package com.nextera.auth;

import cn.hutool.crypto.digest.BCrypt;

/**
 * @author Scout
 * @date 2025-06-17 8:25
 * @since 1.0
 */
public class CheckPassTest {
    public static void main(String[] args) {
//        String  loginPasswd = "$2a$10$7JB720yubVSOfvVOZGVkMe5w2x8KELEyqKCVOqFNlnSYxHCjGJjLm";
        String  loginPasswd = "$2a$10$2/dO7hSjQyf5YnLg92n0NOpvb6X8So2eF2fceQSPnurBE6SNGAz1a";
        String pass = "admin123";
        boolean ret = BCrypt.checkpw(pass, loginPasswd);
        System.out.println(BCrypt.hashpw(pass, BCrypt.gensalt()));
        System.out.println(ret);
    }
}