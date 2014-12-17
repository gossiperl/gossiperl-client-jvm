package com.gossiperl.client.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class Aes256 {

    private SecretKeySpec key;

    public Aes256(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(key.getBytes());
        this.key = new SecretKeySpec(md.digest(), "AES");
    }

    public byte[] encrypt(byte[] data) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] ivBytes = generateIv();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(ivBytes));
        byte[] encrypted = cipher.doFinal(data);
        byte[] complete = new byte[ ivBytes.length + encrypted.length ];
        System.arraycopy(ivBytes, 0, complete, 0, ivBytes.length);
        System.arraycopy(encrypted, 0, complete, ivBytes.length, encrypted.length);
        return complete;
    }

    public byte[] decrypt(byte[] data) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] ivBytes = new byte[16];
        byte[] message = new byte[ data.length - 16 ];
        System.arraycopy(data, 0, ivBytes, 0, ivBytes.length);
        System.arraycopy(data, ivBytes.length, message, 0, message.length);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
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
