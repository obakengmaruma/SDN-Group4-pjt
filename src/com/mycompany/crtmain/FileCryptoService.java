package com.mycompany.crtmain;

import java.io.IOException;
import java.math.BigInteger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class FileCryptoService {
    
    private final FileHandler fileHandler;
    
    public FileCryptoService() {
        this.fileHandler = new FileHandler();
    }
    
    public boolean encryptFile(String soPath, String desPath, RSAKeyPair keyPair){
        try{
            byte[] fileBytes = fileHandler.readFile(soPath);   //Read input bytes from handler

            if(fileBytes == null || fileBytes.length == 0){   //Edge case(empty file)
                System.out.println("[Edge case] detected:");
                System.out.println("File cannot be read or is empty");
                System.out.println("Writing Empty file to disk");
                fileHandler.writeFile(desPath, new byte[0]);
                return true;
            } 

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");    //AES encryption Payload
            keyGen.init(256);
            SecretKey aesKey = keyGen.generateKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] aesPayload = cipher.doFinal(fileBytes);

            byte[] aesKeyByte = aesKey.getEncoded();    //RSA encryption AES Key
            BigInteger aesKBI = new BigInteger(1, aesKeyByte);
            BigInteger rsaEncypt = aesKBI.modPow(keyPair.getE(), keyPair.getN());

            byte[] rsaKeyByte = rsaEncypt.toByteArray();  //formatting RSA key
            byte[] rsaKBF = new byte[256];
            if (rsaKeyByte.length > 256){
                System.arraycopy(rsaKeyByte, rsaKeyByte.length - 256, rsaKBF, 0, 256);
            }else if (rsaKeyByte.length < 256){
                System.arraycopy(rsaKeyByte, 0, rsaKBF, 256 - rsaKeyByte.length, rsaKeyByte.length);
            }else{
                rsaKBF = rsaKeyByte; 
            }

            byte[] rsaAES = new byte[256 + aesPayload.length];   //writing to disk
            System.arraycopy(rsaKBF, 0, rsaAES, 0, 256);
            System.arraycopy(aesPayload, 0, rsaAES, 256, aesPayload.length);
            fileHandler.writeFile(desPath, rsaAES);
            System.out.println("Encryption was successful: ");
            System.out.println("File will be saved: " + desPath);
            return true;
            
        
        }catch(IOException e) { 
            System.out.println("File I/O Error has occured: " + e.getMessage());
            System.out.println("Encryption was Unsuccessful");
            return false;    
        }catch(Exception e) { 
            System.out.println("Cryptographic Error has occured: " + e.getMessage());
            System.out.println("Encryption was Unsuccessful");
            return false;
        }
    }
    
    public boolean decryptFile(String soPath, String desPath, RSAKeyPair keyPair){
      try{
          byte[] cipherBytes = fileHandler.readFile(soPath);  //read encrypted bytes from fileHandler 
          
          if(cipherBytes == null || cipherBytes.length < 256){
                System.out.println("[Edge case] detected:");
                System.out.println("Encrypted file cannot be read or is empty");
                System.out.println("Writing Empty file to disk");
                fileHandler.writeFile(desPath, new byte[0]);
                return true;
            }

            byte[] rsaEnKey = new byte[256];    //spearate RSA key from payload
            byte[] aesEnPayload = new byte[cipherBytes.length - 256];
            System.arraycopy(cipherBytes, 0, rsaEnKey, 0, 256); 
            System.arraycopy(cipherBytes, 256, aesEnPayload, 0, cipherBytes.length - 256);

            BigInteger posRSAEnKey = new BigInteger(1, rsaEnKey);   //Decrypt AES Key with CRT
            CRTDecryptEngine crtEng = new CRTDecryptEngine(keyPair.getP(), keyPair.getQ(), keyPair.getDp(), keyPair.getDq(), keyPair.getQInv());
            BigInteger crtDeRSAEnKey = crtEng.decryptCRT(posRSAEnKey);    
            byte[] rawDeKey = crtDeRSAEnKey.toByteArray();
            byte[] crtRSAKey = new byte[32];
            if (rawDeKey.length > 32){
                System.arraycopy(rawDeKey, rawDeKey.length - 32, crtRSAKey, 0, 32);
            }else if (rawDeKey.length < 32){
                System.arraycopy(rawDeKey, 0, crtRSAKey, 32 - rawDeKey.length, rawDeKey.length);
            }else {
                crtRSAKey = rawDeKey;
            }

            SecretKey secKey = new SecretKeySpec(crtRSAKey, "AES"); //Decrypt Payload
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secKey);
            byte[] aesDePay = cipher.doFinal(aesEnPayload);

            fileHandler.writeFile(desPath, aesDePay);
            System.out.println("Decryption was successful");
            System.out.println("File saved: " + desPath);
            return true;
          
      }catch (IOException e){
          System.out.println("File I/O Error has occured: " + e.getMessage());
          System.out.println("Decryption was Unsuccessful");
          return false;
      }catch (Exception e){
          System.out.println("Cryptographic Error has occured: " + e.getMessage());
          System.out.println("Decryption was Unsuccessful");
          return false;
      }  
    } 
}