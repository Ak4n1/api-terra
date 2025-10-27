package com.ak4n1.terra.api.terra_api.game.utils;

import java.security.MessageDigest;
import java.util.Base64;

public class L2ClientPasswordEncoder {
    public static String encodePassword(String password) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(password.getBytes("UTF-8"));
        // El pack('H*', sha1) es la versión binaria del hash hex, acá ya está en binario
        return Base64.getEncoder().encodeToString(sha1Hash);
    }


}
