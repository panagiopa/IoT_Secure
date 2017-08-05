package com.upatras.android.iot_secure;

/**
 * Created by elite on 19-Jun-17.
 */

import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESGCM extends OpenSSLDecryptor {

    byte[] bkey;

    AESGCM(String key) {
        //if (key.length != 32) throw new IllegalArgumentException();
        this.bkey = hexStringToByteArray(key);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    // the output is sent to users
    byte[] encrypt(byte[] src,String invocation,byte[] AAD) throws Exception {
        //SecretKeySpec key = new SecretKeySpec(this.bkey, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] salt = hexStringToByteArray(invocation);
      // byte[] iv = cipher.getIV(); // TODO Create my RANDOM IV??
        MessageDigest md = MessageDigest.getInstance("SHA256");
        final byte[][] keyAndIV = EVP_BytesToKey(
                KEY_SIZE_BITS / Byte.SIZE,
                12,
                md,
                salt,
                this.bkey,
                ITERATIONS);
Log.e("AESGCM",Integer.toString(cipher.getBlockSize()));
        //byte[] iv = {-90, 49, 70, -80, -1, 100, -85, 11, -63, -64, 93, -81};//,32,33,34,35};
        SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");
        IvParameterSpec iv = new IvParameterSpec(keyAndIV[INDEX_IV]);
        Log.e("AESGCM", "KEY="+bytesToHex(keyAndIV[INDEX_KEY]));
       // IvParameterSpec iv1 = new IvParameterSpec(iv);
       // assert iv.length == 12; // 12 byte = 96bits
        Log.e("AESGCM", "IV="+bytesToHex(keyAndIV[INDEX_IV]));
        byte[] tag = new byte[16];

        //Log.e("AESGCM", "ADD="+bytesToHex(add));
        cipher.init(Cipher.ENCRYPT_MODE, key,iv);

        cipher.updateAAD(AAD);
        byte[] cipherText = cipher.doFinal(src);

        Log.e("AESGCM", "cipherText= " + Base64.encodeToString(cipherText, Base64.DEFAULT));
        System.arraycopy(cipherText, cipherText.length-16, tag, 0,16);
        Log.e("AESGCM", "TAG="+bytesToHex(tag));
        assert cipherText.length == src.length + 16; // 16bytes = 128bits tag
        byte[] message = new byte[12 + src.length + 16]; // TODO check stackoverflow https://stackoverflow.com/questions/31851612/java-aes-gcm-nopadding-what-is-cipher-getiv-giving-me?noredirect=1&lq=1
        System.arraycopy(keyAndIV[INDEX_IV], 0, message, 0, 12);
        System.arraycopy(cipherText, 0, message, 12, cipherText.length);
        return cipherText;
    }

    // the input comes from users
    byte[] decrypt(byte[] message,String invocation) throws Exception {
        //SecretKeySpec key = new SecretKeySpec(this.bkey, "AES");
        if (message.length < 12 + 16) throw new IllegalArgumentException();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] salt = hexStringToByteArray(invocation);
        MessageDigest md = MessageDigest.getInstance("SHA256");
        final byte[][] keyAndIV = EVP_BytesToKey(
                KEY_SIZE_BITS / Byte.SIZE,
                cipher.getBlockSize(),
                md,
                salt,
                this.bkey,
                ITERATIONS);

        //byte[] iv = {-90, 49, 70, -80, -1, 100, -85, 11, -63, -64, 93, -81};//,32,33,34,35};
        SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], "AES");

       // byte[] iv = {-90, 49, 70, -80, -1, 100, -85, 11, -63, -64, 93, -81};//,32,33,34,35};
       // IvParameterSpec iv1 = new IvParameterSpec(iv);
       // assert iv.length == 12; // 12 byte = 96bits
     //   Log.e("AESGCM", "IV="+Arrays.toString(iv));
        //byte[] tag = new byte[16];
     //   byte[] add = "012345678901".getBytes();
    //    Log.e("AESGCM", "ADD="+Arrays.toString(iv));
        GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);

        cipher.init(Cipher.DECRYPT_MODE, key, params);
        byte[] add = "01234567890111111".getBytes();
        cipher.updateAAD(add);
       // byte[] tag = new byte[16];
      //  System.arraycopy(message, message.length-16, tag, 0, 16);
      //  byte[] test = new byte[message.length-16];
      //  System.arraycopy(message, 0, test, 0, message.length-16);
      //  Log.e("AESGCM", "TEST="+Arrays.toString(test));
      //  cipher.updateAAD(tag);
     //   Log.e("AESGCM", "TAG="+Arrays.toString(tag));
        return cipher.doFinal(message, 12, message.length - 12);
        //return cipher.doFinal(test);
    }


}

