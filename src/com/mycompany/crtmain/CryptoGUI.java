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

    private static final String VAULT_FILE = "keyvault.dat";
    private static final String KEY_ALIAS = "default";
    private KeyVault vault;

    private void logVault(ActivityLogger.Status s, String d) {
    ActivityLogger.log(ActivityLogger.Action.KEY_VAULT, s, d);
    }

    private String fingerprint(RSAKeyPair k) {
    try {
        byte[] h = java.security.MessageDigest.getInstance("SHA-256").digest(k.getN().toByteArray());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(String.format("%02X", h[i])).append(i < 7 ? ":" : "");
        return sb.toString();
    } catch (Exception e) {
        return "n/a";
    }
    }

    private char[] askPassword(String message) {
    JPasswordField field = new JPasswordField(20);
    int r = JOptionPane.showConfirmDialog(frame, new Object[]{message, field}, "Key Vault",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    return r == JOptionPane.OK_OPTION ? field.getPassword() : null;
    }

    private boolean unlock() {
    if (vault != null) return true;
    boolean isNew = !Files.exists(Paths.get(VAULT_FILE));
    char[] pw = askPassword(isNew ? "Create a master password (it cannot be recovered):" : "Enter master password:");
    if (pw == null || pw.length == 0) return false;
    if (isNew) {
        char[] again = askPassword("Confirm master password:");
        if (again == null || !java.util.Arrays.equals(pw, again)) {
            say("Passwords did not match.");
            logVault(ActivityLogger.Status.FAILURE, "Vault setup cancelled: passwords did not match");
            return false;
        }
        java.util.Arrays.fill(again, '\0');
    }
    KeyVault v = new KeyVault(VAULT_FILE, pw);
    java.util.Arrays.fill(pw, '\0');
    try {
        v.getKey(KEY_ALIAS);
    } catch (Exception ex) {
        say("Could not open vault: wrong password or corrupted file.");
        logVault(ActivityLogger.Status.FAILURE, "Vault unlock failed: wrong password or corrupted file");
        return false;
    }
    vault = v;
    logVault(ActivityLogger.Status.SUCCESS, isNew ? "Vault master password set" : "Vault unlocked");
    return true;
}

    private void openVault() {
    if (!unlock()) return;
    String[] options = {"Save current keys", "Load saved keys", "View current keys", "Lock vault"};
    int c = JOptionPane.showOptionDialog(frame, "Choose an action", "Key Vault",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    try {
        if (c == 0) {
            if (keypair == null) {
                say("No keys to save. Generate keys first.");
                return;
            }
            vault.storeKey(KEY_ALIAS, keypair);
            say("Key pair saved to vault.");
            logVault(ActivityLogger.Status.SUCCESS, "Key pair saved to vault, fingerprint " + fingerprint(keypair));
        } else if (c == 1) {
            Object k = vault.getKey(KEY_ALIAS);
            if (!(k instanceof RSAKeyPair)) {
                say("No key pair stored in the vault yet.");
                logVault(ActivityLogger.Status.INFO, "Load attempted: vault holds no key pair");
                return;
            }
            if (keypair != null && JOptionPane.showConfirmDialog(frame,
                    "This replaces the keys currently loaded. Continue?", "Key Vault",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            keypair = (RSAKeyPair) k;
            say("Key pair loaded from vault.");
            logVault(ActivityLogger.Status.SUCCESS, "Key pair loaded from vault, fingerprint " + fingerprint(keypair));
        } else if (c == 2) {
            if (keypair == null) {
                say("No keys loaded. Generate or load keys first.");
                return;
            }
            JTextArea area = new JTextArea(
                "Key size: " + keypair.getN().bitLength() + " bits\n" +
                "Fingerprint: " + fingerprint(keypair) + "\n" +
                "Public exponent (e): " + keypair.getE() + "\n\n" +
                "Modulus (n), hex:\n" + keypair.getN().toString(16));
            area.setEditable(false);
            area.setLineWrap(true);
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(480, 220));
            JOptionPane.showMessageDialog(frame, sp, "Current Key Pair (public part)", JOptionPane.INFORMATION_MESSAGE);
            logVault(ActivityLogger.Status.INFO, "Key pair viewed (public part), fingerprint " + fingerprint(keypair));
        } else if (c == 3) {
            vault.wipeMasterPassword();
            vault = null;
            say("Vault locked.");
            logVault(ActivityLogger.Status.INFO, "Vault locked");
        }
    } catch (Exception ex) {
        say("Vault error: " + ex.getMessage());
        logVault(ActivityLogger.Status.FAILURE, "Vault error: " + ex.getMessage());
    }
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
        JPanel buttons = new JPanel(new GridLayout(6, 1, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //this are the buttons
        JButton genKeys = new JButton("1. Generate Keys");
        JButton encrBtn = new JButton("2. Encrypt File");
        JButton decrBtn = new JButton("3. Decrypt File");
        JButton KeyVbtn = new JButton("4. View Keys vault");
        JButton logBtn = new JButton("5. View Activity Log");
        JButton exit = new JButton("6. Exit");

        buttons.add(genKeys);
        buttons.add(encrBtn);
        buttons.add(decrBtn);
        buttons.add(KeyVbtn);
        buttons.add(logBtn);
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
                ActivityLogger.log(ActivityLogger.Action.KEY_GENERATION, ActivityLogger.Status.SUCCESS, "New RSA key pair generated");
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

        KeyVbtn.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e){
                openVault();
            }
        });

        logBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea logArea = new JTextArea(ActivityLogger.getLogAsText());
                logArea.setEditable(false);
                JOptionPane.showMessageDialog(frame, new JScrollPane(logArea), "Activity Log", JOptionPane.INFORMATION_MESSAGE);
                ActivityLogger.log(ActivityLogger.Action.VIEW_LOG, ActivityLogger.Status.INFO,"Activity log viewed." );
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
