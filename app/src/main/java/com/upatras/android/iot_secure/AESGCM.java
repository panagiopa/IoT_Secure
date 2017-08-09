package com.upatras.android.iot_secure;

/**
 * Created by elite on 19-Jun-17.
 */

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESGCM extends OpenSSLDecryptor {

    byte[] bkey;

    AESGCM(String key) {
        this.bkey = hexStringToByteArray(key);
        if (this.bkey.length != 16) throw new IllegalArgumentException();
    }

    public void setBkey(String key){
        this.bkey = hexStringToByteArray(key);
        if (this.bkey.length != 16) throw new IllegalArgumentException();
    }

    // the output is sent to users
    byte[] encrypt(byte[] src,String invocation,byte[] AAD) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] salt = hexStringToByteArray(invocation);
        if (salt.length != 8) throw new IllegalArgumentException();
        MessageDigest md = MessageDigest.getInstance("SHA256");
        final byte[][] keyAndIV = EVP_BytesToKey(
                KEY_SIZE_BITS / Byte.SIZE,
                12,
                md,
                salt,
                this.bkey,
                ITERATIONS);

        SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");
        IvParameterSpec iv = new IvParameterSpec(keyAndIV[INDEX_IV]);

        cipher.init(Cipher.ENCRYPT_MODE, key,iv);
        cipher.updateAAD(AAD);

        return cipher.doFinal(src); // TODO check stackoverflow https://stackoverflow.com/questions/31851612/java-aes-gcm-nopadding-what-is-cipher-getiv-giving-me?noredirect=1&lq=1

    }

    // the input comes from users
    byte[] decrypt(byte[] cipherText,String invocation,byte[] AAD) throws Exception {

        if (cipherText.length < 12 + 16) throw new IllegalArgumentException();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] salt = hexStringToByteArray(invocation);
        if (salt.length != 8) throw new IllegalArgumentException();
        MessageDigest md = MessageDigest.getInstance("SHA256");
        final byte[][] keyAndIV = EVP_BytesToKey(
                KEY_SIZE_BITS / Byte.SIZE,
                cipher.getBlockSize(),
                md,
                salt,
                this.bkey,
                ITERATIONS);

        SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");

        byte[] cipherwithIV = new byte[12 + cipherText.length];
        System.arraycopy(keyAndIV[INDEX_IV], 0, cipherwithIV, 0, 12);
        System.arraycopy(cipherText, 0, cipherwithIV, 12, cipherText.length);

        GCMParameterSpec params = new GCMParameterSpec(128, cipherwithIV, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        cipher.updateAAD(AAD);

        return cipher.doFinal(cipherwithIV, 12, cipherwithIV.length - 12);
    }
}

