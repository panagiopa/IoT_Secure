package com.upatras.android.iot_secure;

/**
 * Created by elite on 19-Jun-17.
 */

import android.util.Log;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESGCM extends OpenSSLDecryptor {

    byte[] bkey;

    AESGCM(String key) {
        //if (key.length != 32) throw new IllegalArgumentException();
        this.bkey = hexStringToByteArray(key);
    }

    // the output is sent to users
    byte[] encrypt(byte[] src) throws Exception {
        SecretKeySpec key = new SecretKeySpec(this.bkey, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
       // byte[] iv = cipher.getIV(); // TODO Create my RANDOM IV??
        byte[] iv = {-90, 49, 70, -80, -1, 100, -85, 11, -63, -64, 93, -81};//,32,33,34,35};
        assert iv.length == 12; // 12 byte = 96bits
        Log.e("AESGCM", "IV="+Arrays.toString(iv));
        byte[] tag = new byte[16];
        cipher.updateAAD(tag);
        byte[] cipherText = cipher.doFinal(src);
        assert cipherText.length == src.length + 16; // 16bytes = 128bits tag
        byte[] message = new byte[12 + src.length + 16]; // TODO check stackoverflow https://stackoverflow.com/questions/31851612/java-aes-gcm-nopadding-what-is-cipher-getiv-giving-me?noredirect=1&lq=1
        System.arraycopy(iv, 0, message, 0, 12);
        System.arraycopy(cipherText, 0, message, 12, cipherText.length);
        return message;
    }

    // the input comes from users
    byte[] decrypt(byte[] message) throws Exception {
        SecretKeySpec key = new SecretKeySpec(this.bkey, "AES");
        if (message.length < 12 + 16) throw new IllegalArgumentException();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        return cipher.doFinal(message, 12, message.length - 12);
    }


}

