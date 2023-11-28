package Client;

import Interface.BulletinBoard;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.List;


public class Main_Client {
    private BulletinBoard bulletinBoard;
    private final String name;
    private final int boardSize = 16; //TODO: is nu hardcoded da moet nog aangepast worden

    private Connection connection;
    private final Map<String, Connection> connections = new HashMap<>();

    private JTextArea textArea;
    private DefaultListModel<String> listModel;

    public Main_Client(String name){
        this.name = name;
        startClient();
    }

    private void startClient() {
        try {
            Registry myRegistry = LocateRegistry.getRegistry("localhost", 1099);
            bulletinBoard = (BulletinBoard) myRegistry.lookup("BulletinBoard");
            createGUI();

        } catch (Exception e) { e.printStackTrace();
        }
    }

    private void createGUI() {
        JFrame f = new JFrame("Chat: " + name);
        JPanel p = new JPanel();
        textArea = new JTextArea(10, 20);
        JTextField textField1 = new JTextField(16);

        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        p.add(textField1);
        p.add(scrollPane);

        // Add a JList for names
        listModel = new DefaultListModel<>();
        JList<String> nameList = new JList<>(listModel);
        JScrollPane nameScrollPane = new JScrollPane(nameList);
        p.add(nameScrollPane);

        // List selection listener for names
        nameList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = nameList.getSelectedValue();
                if (selectedName != null && !selectedName.equals(connection.getName_other())) {
                    textArea.setText("");
                    connection = connections.get(selectedName);
                    List<String> history = connection.getHistory();
                    for (int i = history.size()-1; i >= 0; i--){
                        try {
                            textArea.getDocument().insertString(0, history.get(i), null);
                        } catch (BadLocationException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        });

        f.add(p);

        textField1.addActionListener(e -> {
            try {
                String text = textField1.getText();
                send(text);
                String fullText = "[" + name + "]" + ": " + text + "\n";
                textArea.append(fullText);
                connection.addToHistory(fullText);
                textField1.setText("");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        f.setSize(400, 400);
        f.setVisible(true);
    }

    public Connection setup(String name_other) throws Exception {
        listModel.addElement(name_other);

        //generate key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();

        //generate current index & tag:
        int currentIndex = new SecureRandom().nextInt(boardSize);
        String currentTag = generateNewTag();

        Connection newConnection = new Connection();
        connections.put(name_other, newConnection);
        connection = newConnection;
        connection.setAttributes_mine(currentIndex, currentTag, key);
        connection.setName_other(name_other);

        return connection;
    }

    public void setConnection(Connection c){
        SecretKey decodedKey = c.getKey_mine();
        connection.setAttributes_other(c.getIndex_mine(), c.getTag_mine(), decodedKey);

        new ReadThread(bulletinBoard, connection, this).start();
    }

    private void send(String m) throws Exception {
        if (connection.getKey_mine() == null) return;

        //generate next index & tag
        int nextIndex = new SecureRandom().nextInt(boardSize);
        String nextTag = generateNewTag();

        //message = idx||:||tag||value -> we weten da tag 128 lang is
        String data = nextIndex + ":" + nextTag + m;
        String encryptedData = encrypt(data, connection.getKey_mine());
        bulletinBoard.write(connection.getIndex_mine(), encryptedData, connection.getTag_mine());

        //generate new key
        SecretKey nextKey = deriveKey(connection.getKey_mine());

        connection.setAttributes_mine(nextIndex, nextTag, nextKey);
    }

    public void receive(String encryptedData, Connection connection) {
        try{
            if (encryptedData != null){
                String decryptedData = decrypt(encryptedData, connection.getKey_other());
                String[] data = splitMessage(decryptedData);

                //generate new key
                SecretKey nextKey = deriveKey(connection.getKey_other());

                connection.setAttributes_other(Integer.parseInt(data[0]), data[1], nextKey);
                String text = "[" + connection.getName_other() + "]: " + data[2] + "\n";
                if (this.connection.equals(connection)) {
                    textArea.append(text);
                }
                connection.addToHistory(text);
            }
        }
        catch (Exception ignored){}
    }

    private String[] splitMessage(String s){
        String[] data = new String[3];
        String[] split = s.split(":", 2);
        data[0] = split[0];
        data[1] = split[1].substring(0, 172);
        data[2] = split[1].substring(172);

        return data;
    }

    private String generateNewTag(){
        byte[] randomBytes1 = new byte[128];
        new SecureRandom().nextBytes(randomBytes1);
        return Base64.getEncoder().encodeToString(randomBytes1);
    }

    private String encrypt(String plaintext, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private SecretKey deriveKey(SecretKey key) throws Exception {
        String salt = "salt";
        int iterationCount = 1000;
        int derivedKeyLength = 128;

        // Derive a new key using PBKDF2
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(Base64.getEncoder().encodeToString(key.getEncoded()).toCharArray(), salt.getBytes(), iterationCount, derivedKeyLength);

        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public String getName(){
        return name;
    }
}