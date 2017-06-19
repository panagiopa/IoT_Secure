package com.example.android.iot_secure;

/**
 * Created by elite on 19-Jun-17.
 */

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESGCM {

    Key key;

    AESGCM(byte[] key) {
        if (key.length != 32) throw new IllegalArgumentException();
        this.key = new SecretKeySpec(key, "AES");
    }

    // the output is sent to users
    byte[] encrypt(byte[] src) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV(); // TODO Create my RANDOM IV??
        assert iv.length == 12; // 12 byte = 96bits
        byte[] cipherText = cipher.doFinal(src);
        assert cipherText.length == src.length + 16; // 16bytes = 128bits tag
        byte[] message = new byte[12 + src.length + 16]; // TODO check stackoverflow https://stackoverflow.com/questions/31851612/java-aes-gcm-nopadding-what-is-cipher-getiv-giving-me?noredirect=1&lq=1
        System.arraycopy(iv, 0, message, 0, 12);
        System.arraycopy(cipherText, 0, message, 12, cipherText.length);
        return message;
    }

    // the input comes from users
    byte[] decrypt(byte[] message) throws Exception {
        if (message.length < 12 + 16) throw new IllegalArgumentException();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        return cipher.doFinal(message, 12, message.length - 12);
    }
}

