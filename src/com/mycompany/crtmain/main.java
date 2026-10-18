package com.mycompany.crtmain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class main {
    private static final Scanner scanner = new Scanner(System.in);
   // private static final FileCryptoService service = new FileCryptoService();

    public static void main(String[] args) {
        int choice;
        do {
            Menu();
            choice = readChoice();
            handle(choice);
        } while (choice != 5);
        scanner.close();
    }

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
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
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
                    service.generateKeyPair();
                    break;
                case 4:
                    service.viewActivityLog();
                    break;
                case 5:
                    System.out.println("Exiting the program.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void encrypt() throws IOException {
        Path in = askFile("File to encrypt: ");
        Path out = Paths.get(in + ".enc");
        service.encryptFile(in, out);
        System.out.println("Encrypted to: " + out);
    }

    private static void decrypt() throws IOException {
        Path in = askFile("File to decrypt: ");
        String name = in.toString();
        Path out = Paths.get(name.endsWith(".enc") ? name.substring(0, name.length() - 4) : name + ".dec");
        service.decryptFile(in, out);
        System.out.println("Decrypted to: " + out);
    }

    private static Path askFile(String prompt) {
        System.out.print(prompt);
        Path path = Paths.get(scanner.nextLine().trim());
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Not a valid file: " + path);
        }
        return path;
    }
}