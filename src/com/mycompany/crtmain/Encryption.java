package com.mycompany.crtmain;

public interface Encryption {

    // Encrypts the given data and returns the encrypted byte array
    byte[] encrypt(byte[] data);
    // Decrypts the given data and returns the decrypted byte array
    byte[] decrypt(byte[] data);
}
