package com.mycompany.crtmain;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.*;

class CryptoGUI {
    private final int KEY_SIZE = 2048;
    private final FileCryptoService service = new FileCryptoService();
    private RSAKeyPair keypair;
    private final JTextArea output = new JTextArea(12, 42);
    private final JFrame frame;

    private void say(String message) {
        output.append(message + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }
    
    public CryptoGUI() {
        frame = new JFrame("File Encryption Tool");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        
        // this is the Title of the GUI
        JLabel title = new JLabel("Custom File Encryption tool");
        title.setFont(new Font("arial", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(title, BorderLayout.NORTH);
        
        // adding the Buttons Panel
        JPanel buttons = new JPanel(new GridLayout(4, 1, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //this are the buttons
        JButton genKeys = new JButton("1. Generate Keys");
        JButton encrBtn = new JButton("2. Encrypt File");
        JButton decrBtn = new JButton("3. Decrypt File");
        JButton exit = new JButton("4. Exit");

        buttons.add(genKeys);
        buttons.add(encrBtn);
        buttons.add(decrBtn);
        buttons.add(exit);
        frame.add(buttons, BorderLayout.EAST);
        
        // Output Area
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 12));
        frame.add(new JScrollPane(output), BorderLayout.CENTER);
        
        // Button Action Listeners (what the buttons do when clicked)
        genKeys.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateKeys();
            }

            private void generateKeys() {
                keypair = KeyGenerator.generateKeys(KEY_SIZE);
                say("Keys generated successfully!");
            }
        });
        
        encrBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCrypto(true);
            }
        });
        
        decrBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCrypto(false);
            }
        });
        
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        say("-----Welcome!------\n  Click 'Generate keys' first, then encrypt a file");
    }

    private void doCrypto(boolean encrypt) {
        
        if(keypair == null){
            say("Error: Please generate keys first.");
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(encrypt ? "Select file to encrypt" : "Select file to decrypt");

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            say("No file selected.");
            return;
        }
        
        Path src = chooser.getSelectedFile().toPath();
        Path dest;

        if (encrypt) {
            dest = Paths.get(src.toString() + ".enc");
        } else {
            String name = src.toString();
            if (name.endsWith(".enc")) {
                name = name.substring(0, name.length() - 4);
            }
            int dot = name.lastIndexOf('.');
            int sep = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
            String suggestName = dot > sep ? name.substring(0, dot) + "_DECRYPTED" + name.substring(dot) : name + "_DECRYPTED";
            dest = Paths.get(suggestName);
        }

        if (Files.exists(dest)) {
            say("File already exists: " + dest);
            return;
        }

        if (encrypt) {
            if(service.encryptFile(src.toString(), dest.toString(), keypair)){
                say("File encrypted successfully:\n" + src.getFileName() + "\nSaved as:\n" + dest.getFileName());
            }else{
                say("Error: Encryption failed for file: " + src.getFileName());
            }
        } else {
            if(service.decryptFile(src.toString(), dest.toString(), keypair)){
                say("File decrypted successfully:\n" + src.getFileName() + "\nSaved as:\n" + dest.getFileName());
            }else{
                say("Error: Decryption failed for file: " + src.getFileName());
            }
        }


    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CryptoGUI();
            }
        });
    }
}
