package main.java;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JButton loginButton;
    private Socket socket;
    PrintWriter out;

//    public LoginGUI(Socket socket) {
//        this.socket = socket;
    public LoginGUI() {
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
                    //TODO (zrobione) obsługa zajętego nicku
//                  //if(! doesUsernameExist(username)){
                    dispose(); // Zamknij okno logowania po zalogowaniu
                    //new LobbyGUI(username, socket);
                    LobbyGUI lobbyGUI = new LobbyGUI(username);

//                    }
                    //JOptionPane.showMessageDialog(null, "Nick jest zajęty.", "Błąd logowania", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        setVisible(true);

        // Dodanie WindowListener do obsługi zdarzenia zamknięcia okna
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                //closeWindow();
            }
        });
        
    }

    private void closeWindow() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean doesUsernameExist(String username) {
        try {
            // Tworzymy obiekt PrintWriter do wysłania danych na serwer
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Tworzymy obiekt BufferedReader do odczytu odpowiedzi z serwera
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Tworzymy obiekt JSONObject, aby sformatować zapytanie w formie JSON
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("command", "check_existence");
            jsonRequest.put("username", username);

            // Wysyłamy zapytanie na serwer w formie JSON
            out.println(jsonRequest.toString());

            // Odbieramy odpowiedź z serwera
            String response = in.readLine();

            // Przetwarzamy odpowiedź (możesz przyjąć, że odpowiedź będzie w formacie JSON)
            JSONObject jsonResponse = new JSONObject(response);
            boolean exists = jsonResponse.getBoolean("exists");

            return exists;
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Obsługa błędu - zwracamy false
        }
    }
}