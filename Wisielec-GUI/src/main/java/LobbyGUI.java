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
import java.util.Random;

public class LobbyGUI extends JPanel {
    private JButton[] roomButtons;
    private JLabel usernameLabel;
    private String username;
    private Socket socket;
//    public LobbyGUI(String loginUsername, Socket socket) {
//        this.socket = socket;
    public LobbyGUI(String loginUsername) {
        this.username = loginUsername;
        setLayout(new GridLayout(0, 4, 10, 10)); // GridLayout z 4 kolumnami

        // Pobierz aktualną listę pokojów z serwera
        // Tutaj możesz wykorzystać swoją logikę do pobrania informacji o pokojach z serwera
        // Może to być tablica obiektów reprezentujących pokoje

        // Przykładowe informacje o pokojach (do zastąpienia logiką pobierającą z serwera)
        RoomInfo[] rooms = new RoomInfo[120]; // Tworzenie tablicy na 12 pokojów

        // Inicjalizacja 12 pokojów z losową liczbą graczy
        for (int i = 0; i < 120; i++) {
            rooms[i] = new RoomInfo(i + 1, new Random().nextInt(10));
        }

        //#TODO (zrobione) wykorzystaj te zapytania z metod aby stworzyć tablice z rooms na bazie klasy roomsInfo
//        int availableRoomsCount = getAvailableRoomsCount();
//
//        // Przechowywanie informacji o pokojach
//        RoomInfo[] rooms = new RoomInfo[availableRoomsCount];
//
//        // Pobieranie informacji o ilości graczy w każdym pokoju
//        for (int i = 0; i < availableRoomsCount; i++) {
//            int roomNumber = i + 1; // Numeracja pokojów zaczyna się od 1
//
//            // Pobieranie ilości graczy w danym pokoju
//            int playersInRoom = getPlayersInRoom(roomNumber);
//
//            // Tworzenie obiektu RoomInfo dla danego pokoju
//            RoomInfo roomInfo = new RoomInfo(roomNumber, playersInRoom);
//
//            // Dodawanie RoomInfo do tablicy rooms
//            rooms[i] = roomInfo;
//        }


        // Wyświetl przyciski dla każdego pokoju
        for (RoomInfo room : rooms) {
            JButton roomButton = new JButton("Pokój " + room.getRoomNumber() + " - Graczy: " + room.getNumPlayers());
            roomButton.addActionListener(e -> joinRoom(room.getRoomNumber()));
            add(roomButton);
        }
        createAndShowGUI();
    }


    public void createAndShowGUI() {
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JFrame frame = new JFrame("Lobby");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        frame.add(scrollPane);
        frame.setVisible(true);
    }

    // Zapytanie o ilość dostępnych pokojów i graczy w nich
    private int getAvailableRoomsCount() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("command", "get_available_rooms_count");

            out.println(jsonRequest.toString());

            String response = in.readLine();
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getInt("availableRooms");
        } catch (IOException e) {
            e.printStackTrace();
            return 0; // Obsługa błędu - zwracamy 0
        }
    }

    private int getPlayersInRoom(int roomNumber) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("command", "get_players_in_room");
            jsonRequest.put("roomNumber", roomNumber);

            out.println(jsonRequest.toString());

            String response = in.readLine();
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getInt("playersInRoom");
        } catch (IOException e) {
            e.printStackTrace();
            return 0; // Obsługa błędu - zwracamy 0
        }
    }


    private void joinRoom(int roomNumber) {
        // Tutaj możesz wywołać metodę dołączania do wybranego pokoju
        // np. otwarcie okna WisielecClientGUI dla danego pokoju
//        WisielecClientGUI wisielecClientGUI = new WisielecClientGUI(roomNumber, username, socket);
        WisielecClientGUI wisielecClientGUI = new WisielecClientGUI(roomNumber, username);
        // Umożliwia przekazanie numeru pokoju do WisielecClientGUI,
        // aby serwer wiedział, który pokój obsłużyć
    }


}

// Klasa reprezentująca informacje o pokoju
 class RoomInfo {
    private int roomNumber;
    private int numPlayers;

    public RoomInfo(int roomNumber, int numPlayers) {
        this.roomNumber = roomNumber;
        this.numPlayers = numPlayers;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getNumPlayers() {
        return numPlayers;
    }
}

