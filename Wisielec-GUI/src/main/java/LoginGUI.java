package main.java;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JButton loginButton;
    private Socket socket;
    private JsonObjectReader jsonObjectReader;
    PrintWriter out;

    public LoginGUI(Socket socket) {
        this.socket = socket;
        jsonObjectReader = new JsonObjectReader();

        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLayout(new FlowLayout());

        JLabel usernameLabel = new JLabel("Nick:");
        usernameField = new JTextField(15);
        loginButton = new JButton("Zaloguj");

        add(usernameLabel);
        add(usernameField);
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Proszę podać nick.", "Błąd logowania", JOptionPane.ERROR_MESSAGE);
                } else {
                  if(doesUsernameExist(username)){
                    dispose(); // Zamknij okno logowania po zalogowaniu
                    new LobbyGUI(username, socket);
                    JOptionPane.showMessageDialog(null, "Nick jest poprawny.", "Poprawne logowanie", JOptionPane.INFORMATION_MESSAGE);
                  }else {
                      handleUsernameExistence(username);
                  }
                }
            }
        });
        setVisible(true);

        // Dodanie WindowListener do obsługi zdarzenia zamknięcia okna
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeWindow();
            }
        });

    }

    private void handleUsernameExistence(String username) {
        int option = JOptionPane.showConfirmDialog(
                null,
                "Nick jest zajęty. Czy chcesz spróbować innego?",
                "Błąd logowania",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            // Użytkownik chce spróbować ponownie
            // Możesz dodać dowolne dodatkowe akcje tutaj
            // Na przykład wyczyść pole usernameField i pozwól mu wprowadzić nową nazwę użytkownika.
            new LoginGUI(socket);
            dispose();
        } else {
            // Zamknij okno logowania
            dispose();
        }
    }

    private void closeWindow() {
        dispose();
    }

    private boolean doesUsernameExist(String username) {
        JSONObject jsonResponse = null; // Zadeklaruj przed try, aby była dostępna poza blokiem
        String command = "register";
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            JSONObject jsonRequest = new JSONObject();

            jsonRequest.put("command", command);
            jsonRequest.put("username", username);
            System.out.println("request:" + jsonRequest);

            out.println(jsonRequest.toString());

            jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);

            if (jsonResponse != null) {
                // Przetwarzamy odpowiedź (możesz przyjąć, że odpowiedź będzie w formacie JSON)
                return jsonResponse.getBoolean("success");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false; // Obsługa błędu - zwracamy false
    }

}
