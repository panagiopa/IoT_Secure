package com.upatras.android.iot_secure;

import android.util.Log;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by elite on 31-Jul-17.
 */
// extends OpenSSLDecryptor for EVP
public class AESCBC extends OpenSSLDecryptor {

    byte[] bkey;

    AESCBC(String key) {
        this.bkey = hexStringToByteArray(key);
       // if (key.length != 32) throw new IllegalArgumentException();
        //this.bkey = key;
       // Log.e("AESCBC","1O=" +String.format("%02x",this.bkey));
        Log.e("AESCBC", "KEY="+Arrays.toString(this.bkey));

    }
/*// TODO Create ENCRYPTION for AES CBC Mode
    // the output is sent to users
    byte[] encrypt(byte[] src) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();
        assert iv.length == 12; // 12 byte = 96bits
        byte[] cipherText = cipher.doFinal(src);
        assert cipherText.length == src.length + 16; // 16bytes = 128bits tag
        byte[] message = new byte[12 + src.length + 16];
        System.arraycopy(iv, 0, message, 0, 12);
        System.arraycopy(cipherText, 0, message, 12, cipherText.length);
        return message;
    }
*/

    // the input comes from users decrypt using OPENSSL encryption
    byte[] decrypt(byte[] message) throws Exception {
        // header is "Salted__", ASCII encoded, if salt is being used (the default)
        byte[] salt = Arrays.copyOfRange(
                message, SALT_OFFSET, SALT_OFFSET + SALT_SIZE);
        byte[] encrypted = Arrays.copyOfRange(
                message, CIPHERTEXT_OFFSET, message.length);
        MessageDigest md = MessageDigest.getInstance("SHA256");
        Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // --- create key and IV  ---

        // the IV is useless, OpenSSL might as well have use zero's
        final byte[][] keyAndIV = EVP_BytesToKey(
                KEY_SIZE_BITS / Byte.SIZE,
                aesCBC.getBlockSize(),
                md,
                salt,
                this.bkey,
                ITERATIONS);
        SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");
        IvParameterSpec iv = new IvParameterSpec(keyAndIV[INDEX_IV]);
        aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrypted = aesCBC.doFinal(encrypted);

        return decrypted;
    }


}
