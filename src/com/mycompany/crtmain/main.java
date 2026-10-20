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
                    pause2();
                    break;
                case 2:
                    decrypt();
                    pause2();
                    break;
                case 3:
                    generateKeys();
                    pause2();
                    break;
                case 4:
                    System.out.println("Activity log feature is not implemented yet.");
                    pause();
                    break;
                case 5:
                    System.out.println("Exiting the program.");
                    pause();
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
            
<<<<<<< HEAD
=======
            pause(); 
>>>>>>> 51f271c269254ce89371adf474364ac8d0ac56c9
            return;
        }
        Path in = askFile("File to encrypt: ");
        Path out = Paths.get(in + ".enc");
        service.encryptFile(in.toString(), out.toString(), keypair );
        
    }

    private static void generateKeys() {
        System.out.println("Generating new RSA key pair...");
        pause();
        keypair = KeyGenerator.generateKeys(KEY_SIZE);
        
        System.out.println("New RSA key pair generated.");
       
      pause2();
    }

    
    private static void decrypt() throws IOException {
        if(!hasKeys()) {
            
<<<<<<< HEAD
=======
            pause(); // Pause for 1 second 
>>>>>>> 51f271c269254ce89371adf474364ac8d0ac56c9
            return;
        }

        Path in = askFile("File to decrypt: ");
        String name = in.toString();
        
        if (name.endsWith(".enc")){
            name = name.substring(0, name.length() - 4); // Remove the ".enc" extension
        }
        int dot = name.lastIndexOf('.');
        int sep = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        String suggestName = dot > sep ? name.substring(0, dot) + "_DECRYPTED" + name.substring(dot) : name + "_DECRYPTED";

        System.out.print("Save file as [" + suggestName + "]: ");
        String outName = scanner.nextLine().trim();
        Path out = Paths.get(outName.isEmpty() ? suggestName : outName);

        if(Files.exists(out)){
            System.out.println("File already exists: " + out);
            return;
        }

        service.decryptFile(in.toString(), out.toString(), keypair);
        pause();
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
            Thread.sleep(1000); // Pause for 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

        private static void pause2(){
        try { 
            Thread.sleep(500); // Pause for 0.5 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}