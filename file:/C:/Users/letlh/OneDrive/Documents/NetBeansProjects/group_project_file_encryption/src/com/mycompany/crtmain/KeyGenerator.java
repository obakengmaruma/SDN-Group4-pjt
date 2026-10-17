package com.mycompany.crtmain;

public class KeyGenerator {
    public static RSAKeyPair generateKeys(int bitLength) {
        return new RSAKeyPair(bitLength);
    }
}
