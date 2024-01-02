package main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

public class LobbyGUI extends JFrame {
    private JButton[] roomButtons;
    private JLabel usernameLabel;
    private String username;
    private Socket socket;
    public LobbyGUI(String loginUsername, Socket socket) {
        this.username = loginUsername;
        this.socket = socket;
        setTitle("Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLayout(new FlowLayout());

        usernameLabel = new JLabel("Użytkownik: " + username);
        usernameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(usernameLabel, BorderLayout.NORTH);

        //TODO odpytywanie serwera o ilość pokojów
        int numberOfRooms = 4; // Przykładowa liczba pokojów
        roomButtons = new JButton[numberOfRooms];

        // Tworzenie przycisków dla pokojów
        for (int i = 0; i < numberOfRooms; i++) {
            roomButtons[i] = new JButton("Pokój " + (i + 1));
            final int roomNumber = i + 1;
            roomButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    joinRoom(roomNumber); // Metoda dołączania do pokoju
                }
            });
            add(roomButtons[i]);
        }

        setVisible(true);
    }

    private void joinRoom(int roomNumber) {
        // Tutaj możesz wywołać metodę dołączania do wybranego pokoju
        // np. otwarcie okna WisielecClientGUI dla danego pokoju
        WisielecClientGUI wisielecClientGUI = new WisielecClientGUI(roomNumber, username, socket);
        // Umożliwia przekazanie numeru pokoju do WisielecClientGUI,
        // aby serwer wiedział, który pokój obsłużyć
    }


}
