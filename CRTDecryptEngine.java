package com.mycompany.crtmain;

import java.math.BigInteger;


public class CRTDecryptEngine {
    
    /*
    Decryption rsa using the CRT( AKA Chinese Reainder Theorem
    
    in the standard rsa, the math would be like: m=c^d mod n (similar to x=v(mod n) 
            the gives one exponentiation of sixe n
    
    using CRT rsa, the math is :m1=c^dp(mod p) -- two exponentiations of half size
                               :m2 = c^dq(mod q) --this makes the decryptor 4 times faster then the standard rsa
    this is the Garner's formula:m = m2+q*[qInv*(m1-m2)mod p]
    */
    
    private final BigInteger p;
    private final BigInteger q;
    private final BigInteger dp;
    private final BigInteger dq;
    private final BigInteger qInv;
    
    public CRTDecryptEngine(BigInteger p, BigInteger q, BigInteger dp, BigInteger dq, BigInteger qInv){
        this.dp=dp;
        this.dq=dq;
        this.p=p;
        this.q=q;
        this.qInv=qInv;
    }
    
        public BigInteger decryptCRT(BigInteger c) {
        BigInteger m1 = c.modPow(dp, p);
        BigInteger m2 = c.modPow(dq, q);
        return chineseRemainderCombine(m1, m2);
        }

    private BigInteger chineseRemainderCombine(BigInteger m1, BigInteger m2) {
        BigInteger diff = m1.subtract(m2);
        BigInteger h = qInv.multiply(diff).mod(p);
        BigInteger x = m2.add(q.multiply(h));
        
        return x;
    }
}