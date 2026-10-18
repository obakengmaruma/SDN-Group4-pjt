package com.mycompany.crtmain;
//package fileencryption.service;  //Group package structure
import java.io.IOException;
import java.math.BigInteger;
import com.mycompany.crtmain.Encryption;   //Import for person 4's class
import com.mycompany.crtmain.RSAKeyPair;  //Import for person 2's class
import com.mycompany.crtmain.CRTDecryptEngine;  //Import for person 3's class
import com.mycompany.crtmain.FileHandler;

public class FileCryptoService {
    
    private final FileHandler fileHandler;
    
    public FileCryptoService() {
        this.fileHandler = new FileHandler();
    }
    
    //soPath = path to the encrypted file
    //desPath = path to where encrypted file fill be saved
    //keyPair = person 2's RSA keypair
    //Booelan = True if encryption was successful or False if not    
    
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
            
            BigInteger oriBytes = new BigInteger(1, fileBytes);  //Convert original bytes to Bigint
            
            BigInteger outBytes = oriBytes.modPow(
                    keyPair.getE(), 
                    keyPair.getN()); //RSA ENcryption using person 2's key genration
            
            fileHandler.writeFile(desPath, outBytes.toByteArray());  //Writes cyfered bytes to destination using fileHandler
            System.out.println("Encryption was successful: ");
            System.out.println("File will be saved: " + desPath);
            return true;
        
        }catch(IOException e) { 
            System.out.println("File I/O Error has occured: ");
            System.out.println(e.getMessage());
            System.out.println("Encryption was Unsuccessful");
            return false;    
        }catch(Exception e) { 
            System.out.println("Cryptographic Error has occured: ");
            System.out.println(e.getMessage());
            System.out.println("Encryption was Unsuccessful");
            return false;
        }
    }
    
    //soPath = path to encrypted file
    //desPath = file to where decrypted file will be saved
    //keyPair = person 2's RSA keypair
    //Booelan = True if decryption was successful or False if not
    
    public boolean decryptFile(String soPath, String desPath, RSAKeyPair keyPair){
      try{
          byte[] cipherBytes = fileHandler.readFile(soPath);  //read encrypted bytes from fileHandler 
          
          if(cipherBytes == null || cipherBytes.length == 0){
                System.out.println("[Edge case] detected:");
                System.out.println("Encrypted file cannot be read or is empty");
                System.out.println("Writing Empty file to disk");
                fileHandler.writeFile(desPath, new byte[0]);
                return true;
            }
          
          BigInteger ciBytes = new BigInteger(1, cipherBytes);  //Convert bytes to BigInt block
          
          CRTDecryptEngine crtEngine = new CRTDecryptEngine(
                  keyPair.getP(),
                  keyPair.getQ(),
                  keyPair.getDp(),
                  keyPair.getDq(),
                  keyPair.getQInv());
          
          BigInteger decBigInt = crtEngine.decryptCRT(ciBytes);  //CRT Decryption
          
          byte[] decBytes = decBigInt.toByteArray();  //Decrypted BigInt back to raw bytes
          
          if (decBytes[0] == 0 && decBytes.length > 1){
              byte[] trimBytes = new byte[decBytes.length - 1];
              System.arraycopy(decBytes, 1, trimBytes, 0, trimBytes.length);
              decBytes = trimBytes;
          }
          
          fileHandler.writeFile(desPath, decBytes);
          System.out.println("Decryption was successful");
          System.out.println("File saved: " + desPath);
          return true;
          
      }catch (IOException e){
          System.out.println("File I/O Error has occured: ");
          System.out.println(e.getMessage());
          System.out.println("Decryption was Unsuccessful");
          return false;
      }catch (Exception e){
          System.out.println("Cryptographic Error has occured: ");
          System.out.println(e.getMessage());
          System.out.println("Decryption was Unsuccessful");
          return false;
      }  
    } 
}

