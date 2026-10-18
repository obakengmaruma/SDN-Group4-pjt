package com.mycompany.crtmain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {

    // Constant buffer size specified in the system design UML (8192 bytes / 8 KB)
    private static final int BUFFER = 8192;
    public FileHandler() {
        // Initialization logic if needed
    }

    public byte[] readFile(String filePath) throws IOException {
        // Convert the string path to a java.nio.file.Path object for modern file checking
        Path path = Paths.get(filePath);

        // Validate if the file actually exists before attempting to open a stream
        if (!Files.exists(path)) {
            throw new IOException("Error: File not found at path -> " + filePath);
        }

        // Validate if the path points to a regular file and not a directory
        if (!Files.isRegularFile(path)) {
            throw new IOException("Error: Specified path is not a valid file -> " + filePath);
        }

        // Use Files.readAllBytes for clean, safe reading of standard files with built-in exception handling
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            // Re-throw with custom contextual error message for tracking
            throw new IOException("Failed to read file contents: " + filePath, e);
        }
    }

    public void writeFile(String destinationPath, byte[] data) throws IOException {
        
        if (data == null) {
            throw new IllegalArgumentException("Error: Cannot write null data to file.");
        }

        Path path = Paths.get(destinationPath);

        // Ensure parent directories exist; create them if they are missing
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        
        try {
            Files.write(path, data);
        } catch (IOException e) {
            throw new IOException("Failed to write data to destination file: " + destinationPath, e);
        }
    }

    public void processFileWithBuffer(String sourcePath, String destinationPath) throws IOException {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destinationPath);

        // Check if source file exists
        if (!sourceFile.exists()) {
            throw new IOException("Source file does not exist: " + sourcePath);
        }

        // Try-with-resources block ensures that streams are automatically and safely closed 
        // even if an exception occurs during reading or writing.
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {

            // Allocate buffer array of size 8192 as defined in UML[cite: 1]
            byte[] bufferArray = new byte[BUFFER];
            int bytesRead;

            // Read chunks of data into the buffer iteratively until EOF (-1) is reached
            while ((bytesRead = fis.read(bufferArray)) != -1) {
                fos.write(bufferArray, 0, bytesRead);
            }
            
            // Flush the output stream to ensure all buffered bytes are written to disk
            fos.flush();

        } catch (IOException e) {
            throw new IOException("An I/O error occurred during buffered file processing.", e);
        }
    }

   
    public int getBuffer() {
        return BUFFER;
    }
}
