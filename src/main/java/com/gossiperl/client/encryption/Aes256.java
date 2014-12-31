package com.gossiperl.client.encryption;

import org.apache.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;

public class Aes256 {

    private SecretKeySpec key;
    private static Logger LOG = Logger.getLogger(Aes256.class);

    public Aes256(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digestBytes = md.digest(key.getBytes("utf-8"));
        this.key = new SecretKeySpec(digestBytes, "AES");
    }

    public byte[] encrypt(byte[] data) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        byte[] ivBytes = generateIv();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(ivBytes));
        try {
            byte[] encrypted = cipher.doFinal(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(ivBytes);
            outputStream.write(encrypted);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }

    public byte[] decrypt(byte[] data) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        byte[] ivBytes = new byte[16];
        byte[] message = new byte[ data.length - 16 ];
        System.arraycopy(data, 0, ivBytes, 0, ivBytes.length);
        System.arraycopy(data, ivBytes.length, message, 0, message.length);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(ivBytes));
        return cipher.doFinal(message);
    }

    private byte[] generateIv() {
        SecureRandom random = new SecureRandom();
        byte[] ivBytes = new byte[16];
        random.nextBytes(ivBytes);
        return ivBytes;
    }

}
