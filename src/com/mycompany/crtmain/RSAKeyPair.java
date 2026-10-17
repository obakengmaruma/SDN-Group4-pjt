
package com.mycompany.crtmain;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSAKeyPair {
    private final BigInteger p ;
    private final BigInteger q ;
    private final BigInteger d ;
    private final BigInteger n;
    private final BigInteger e;
    private final BigInteger dp;
    private final BigInteger dq;
    private final BigInteger qInv;
    private final BigInteger phi;
  public RSAKeyPair (int bitLength)
    {
    SecureRandom randomNumber = new SecureRandom();
    this.p = BigInteger.probablePrime(bitLength/2, randomNumber);
    this.q = BigInteger.probablePrime(bitLength/2, randomNumber);
    this.n = p.multiply(q);
    this.phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
    this.e =BigInteger.valueOf(65537);
    this.d = e.modInverse(phi);
    this.dp = d.mod(p.subtract(BigInteger.ONE));
    this.dq = d.mod(q.subtract(BigInteger.ONE));
    this.qInv = q.modInverse(p);
    }
    //returning every calculated value to use for encryption
    public BigInteger getP()
    {
        return p;
    }
    public BigInteger getQ()
    {
        return q;
    }
    public BigInteger getN()
    {
        return n;
    }
    public BigInteger getE()
    {
        return e;
    }
    public BigInteger getD()
    {
        return d;
    }
    public BigInteger getDp()
    {
        return dp;
    }
    public BigInteger getDq()
    {
        return dq;
    }
    public BigInteger getQInv()
    {
        return qInv;
    }

}
