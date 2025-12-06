package cybersecuritydashboard;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class FileEncryptPanel extends JPanel {

    private CyberSecurityDashBoard parent;

    private JTextField fileField;
    private JPasswordField passwordField;
    private JButton browseButton, encryptButton, decryptButton;
    private JTextArea logArea;

    public FileEncryptPanel(CyberSecurityDashBoard parent) {
        this.parent = parent;

        // main style
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(15, 15, 20));

        // top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(new Color(25, 25, 35));

        JLabel title = new JLabel("File Encryption");
        title.setForeground(new Color(0, 200, 255));
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(title);

        top.add(new JLabel("File:"));
        fileField = new JTextField(22);
        fileField.setBackground(new Color(35, 35, 45));
        fileField.setForeground(Color.WHITE);
        top.add(fileField);

        browseButton = new JButton("Browse");
        top.add(browseButton);

        top.add(new JLabel("Password:"));
        passwordField = new JPasswordField(12);
        passwordField.setBackground(new Color(35, 35, 45));
        passwordField.setForeground(Color.WHITE);
        top.add(passwordField);

        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        top.add(encryptButton);
        top.add(decryptButton);

        add(top, BorderLayout.NORTH);

        // log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(10, 10, 15));
        logArea.setForeground(new Color(230, 230, 230));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                "Encryption / Decryption Log",
                0, 0,
                new Font("Segoe UI", Font.PLAIN, 12),
                new Color(180, 180, 200)
        ));
        add(scroll, BorderLayout.CENTER);

        // actions
        browseButton.addActionListener(e -> chooseFile());
        encryptButton.addActionListener(e -> encryptSelectedFile());
        decryptButton.addActionListener(e -> decryptSelectedFile());
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void encryptSelectedFile() {
        String path = fileField.getText().trim();
        char[] password = passwordField.getPassword();

        if (path.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Select file and enter password");
            return;
        }

        File input = new File(path);
        File output = new File(path + ".enc");
        try {
            processFile(Cipher.ENCRYPT_MODE, password, input, output);
            parent.totalFilesEncrypted++; // update counter for Analysis tab
            logArea.append("[+] Encrypted → " + output.getAbsolutePath() + "\n");
        } catch (Exception ex) {
            logArea.append("[!] Error encrypting: " + ex.getMessage() + "\n");
        }
    }

    private void decryptSelectedFile() {
        String path = fileField.getText().trim();
        char[] password = passwordField.getPassword();

        if (path.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Select encrypted file and enter password");
            return;
        }

        File input = new File(path);
        File output;

        if (path.endsWith(".enc")) {
            output = new File(path.substring(0, path.length() - 4) + ".dec");
        } else {
            output = new File(path + ".dec");
        }

        try {
            processFile(Cipher.DECRYPT_MODE, password, input, output);
            logArea.append("[+] Decrypted → " + output.getAbsolutePath() + "\n");
        } catch (Exception ex) {
            logArea.append("[!] Error decrypting: " + ex.getMessage() + "\n");
        }
    }

    // AES CBC with PBKDF2 password-based key derivation – standard approach for demos [web:31][web:142]
    private void processFile(int mode, char[] password, File inFile, File outFile) throws Exception {
        byte[] salt = "12345678".getBytes(); // demo only; explain in report
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        if (mode == Cipher.ENCRYPT_MODE) {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secret, ivSpec);

            try (FileInputStream fis = new FileInputStream(inFile);
                 FileOutputStream fos = new FileOutputStream(outFile)) {

                fos.write(iv); // store IV at file start
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    byte[] out = cipher.update(buffer, 0, read);
                    if (out != null) fos.write(out);
                }
                byte[] outBytes = cipher.doFinal();
                if (outBytes != null) fos.write(outBytes);
            }
        } else { // decrypt
            try (FileInputStream fis = new FileInputStream(inFile);
                 FileOutputStream fos = new FileOutputStream(outFile)) {

                byte[] iv = new byte[16];
                if (fis.read(iv) != 16) throw new IOException("No IV found");
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);

                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    byte[] out = cipher.update(buffer, 0, read);
                    if (out != null) fos.write(out);
                }
                byte[] outBytes = cipher.doFinal();
                if (outBytes != null) fos.write(outBytes);
            }
        }
    }
}
