package com.mycompany.crtmain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyVault {

    // UML Attributes
    private final Path vaultPath;
    private final char[] masterPassword;

    // Cryptographic Parameters
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256; // 256-bit AES key
    private static final int SALT_SIZE = 16;   // 128-bit salt
    private static final int IV_SIZE = 12;     // 96-bit IV for AES-GCM
    private static final int TAG_LENGTH_BIT = 128;

    public KeyVault(Path vaultPath, char[] masterPassword) {
        this.vaultPath = vaultPath;
        this.masterPassword = masterPassword != null ? masterPassword.clone() : new char[0];
    }

    public KeyVault(String vaultPathStr, char[] masterPassword) {
        this(Paths.get(vaultPathStr), masterPassword);
    }

    @SuppressWarnings("unchecked")
    public synchronized void storeKey(String keyAlias, Object keyPair) throws IOException, GeneralSecurityException {
        Map<String, Object> keyStoreMap = loadVaultMap();
        keyStoreMap.put(keyAlias, keyPair);
        saveVaultMap(keyStoreMap);
    }

    public synchronized Object getKey(String keyAlias) throws IOException, GeneralSecurityException {
        Map<String, Object> keyStoreMap = loadVaultMap();
        return keyStoreMap.get(keyAlias);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadVaultMap() throws IOException, GeneralSecurityException {
        File vaultFile = vaultPath.toFile();
        if (!vaultFile.exists() || vaultFile.length() == 0) {
            return new HashMap<>();
        }

        try (FileInputStream fis = new FileInputStream(vaultFile)) {
            // Read Salt & IV
            byte[] salt = new byte[SALT_SIZE];
            byte[] iv = new byte[IV_SIZE];

            if (fis.read(salt) != SALT_SIZE || fis.read(iv) != IV_SIZE) {
                throw new IOException("Corrupted vault header.");
            }

            // Read encrypted ciphertext payload
            byte[] cipherText = fis.readAllBytes();

            // Derive key and decrypt payload
            SecretKey secretKey = deriveKey(masterPassword, salt);
            byte[] decryptedData = performCipherOperation(Cipher.DECRYPT_MODE, secretKey, iv, cipherText);

            // Deserialize Map
            try (ObjectInputStream ois = new ObjectInputStream(new java.io.ByteArrayInputStream(decryptedData))) {
                return (Map<String, Object>) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("Failed to deserialize key vault contents.", e);
            }
        }
    }

    private void saveVaultMap(Map<String, Object> keyStoreMap) throws IOException, GeneralSecurityException {
        // Serialize object map to byte array
        byte[] serializedData;
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(keyStoreMap);
            oos.flush();
            serializedData = baos.toByteArray();
        }

        // Generate random Salt and IV
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        byte[] iv = new byte[IV_SIZE];
        random.nextBytes(salt);
        random.nextBytes(iv);

        // Derive key and encrypt payload
        SecretKey secretKey = deriveKey(masterPassword, salt);
        byte[] cipherText = performCipherOperation(Cipher.ENCRYPT_MODE, secretKey, iv, serializedData);

        // Ensure parent directory exists
        File file = vaultPath.toFile();
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // Write [Salt][IV][CipherText] to vault file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(salt);
            fos.write(iv);
            fos.write(cipherText);
            fos.flush();
        }
    }

    private SecretKey deriveKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        spec.clearPassword();
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private byte[] performCipherOperation(int mode, SecretKey key, byte[] iv, byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(mode, key, parameterSpec);
        return cipher.doFinal(input);
    }

    public void wipeMasterPassword() {
        Arrays.fill(this.masterPassword, '0');
    }

    // Getter for Vault Path
    public Path getVaultPath() {
        return vaultPath;
    }
}