package com.mycompany.crtmain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class main {

    private static final int KEY_SIZE = 2048; // RSA key size in bits
    private static final Scanner scanner = new Scanner(System.in);
   private static final FileCryptoService service = new FileCryptoService();
    private static RSAKeyPair keypair;
// runs the main menu loop 
   public static void main(String[] args) {
        int choice;
        do {
            Menu();
            choice = readChoice();
            handle(choice);
        } while (choice != 5);
        scanner.close();
    }
// displays the menu options to the user
    public static void Menu() {
        System.out.println("\n Custom file Encyption tool menu: ");
        System.out.println("1. Encrypt a file");
        System.out.println("2. Decrypt a file");
        System.out.println("3. Generate a new key-pair");
        System.out.println("4. View activity log");
        System.out.println("5. Exit");
    }

    public static int readChoice() {
        System.out.print("Enter your choice: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim()); // Read user input and convert to integer
        } catch (NumberFormatException e) { 
            return 0;
        } // for invalid input

    }

    public static void handle(int choice) {
        try {
            switch (choice) {
                case 1:
                    encrypt();
                    break;
                case 2:
                    decrypt();
                    break;
                case 3:
                    generateKeys();
                    break;
                case 4:
                    System.out.println("Activity log feature is not implemented yet.");
                    break;
                case 5:
                    System.out.println("Exiting the program.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    pause();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
// prompts the user for a file path and checks if it's a valid file
    private static void encrypt() throws IOException {
        if(!hasKeys()) {
            System.out.println("No RSA key pair found.  Choose Option 3 first");
            return;
        }
        Path in = askFile("File to encrypt: ");
        Path out = Paths.get(in + ".enc");
        service.encryptFile(in.toString(), out.toString(), keypair );
        //System.out.println("Encrypted to: " + out);
    }

    private static void generateKeys() {
        System.out.println("Generating new RSA key pair...");
        keypair = KeyGenerator.generateKeys(KEY_SIZE);
        System.out.println("New RSA key pair generated.");
        //System.out.println("Public Key: " + keypair.getPublicKey());
      //  System.out.println("Private Key: " + keypair.getPrivateKey());
    }

    
    private static void decrypt() throws IOException {
        if(!hasKeys()) {
            System.out.println("No RSA key pair found.  Choose Option 3 first");
            return;
        }

        Path in = askFile("File to decrypt: ");
        String name = in.toString();
        Path out = Paths.get(name.endsWith(".enc") ? name.substring(0, name.length() - 4) : name + ".dec");
        service.decryptFile(in.toString(), out.toString(), keypair);
        //System.out.println("Decrypted to: " + out);
    }

   
    private static Path askFile(String prompt) {
        System.out.print(prompt);
        Path path = Paths.get(scanner.nextLine().trim());
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Not a valid file: " + path);
        }
        return path; // Return the valid file path
    }

    private static boolean hasKeys() {
        if (keypair == null) {
            System.out.println("No RSA key pair found.  Choose Option 3 first");
            return false;
        }
        return true;
    }

    private static void pause(){
        try { 
            Thread.sleep(2000); // Pause for 2 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}