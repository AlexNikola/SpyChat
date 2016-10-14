package com.incode_it.spychat.utils;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class Cypher {

    private static final String PASS = "Tvk8X0Qqg7FwiqPLYvl5";

    public static String encrypt(String msg) {
        try {
            msg = AESCrypt.encrypt(PASS, msg);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return msg;
    }

    public static String decrypt(String msg) {
        try {
            msg = AESCrypt.decrypt(PASS, msg);
        }catch (GeneralSecurityException e){
            e.printStackTrace();
        }

        return msg;
    }
}
