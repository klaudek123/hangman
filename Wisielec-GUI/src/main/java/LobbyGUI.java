package main.java;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class  LobbyGUI extends JPanel {
//    private JButton[] roomButtons;
    private final String username;
    private Socket socket;
    private String command;
    PrintWriter out;
    private final JFrame frame; // Dodane pole do przechowywania referencji do okna
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public LobbyGUI(String username, Socket socket) {
        this.socket = socket;
        this.username = username;
        setLayout(new GridLayout(0, 4, 10, 10)); // GridLayout z 4 kolumnami

        int availableRoomsCount = getAvailableRoomsCount();

        // Przechowywanie informacji o pokojach
        LobbyInfo[] rooms = new LobbyInfo[availableRoomsCount];

        // Pobieranie informacji o ilości graczy w każdym pokoju
        for (int i = 0; i < availableRoomsCount - 1; i++) {
            int roomNumberTmp = i + 1; // Numeracja pokojów zaczyna się od 1

            // Pobieranie ilości graczy w danym pokoju
            int playersInRoom = getPlayersInLobby(roomNumberTmp);
            //int playersInRoom = new Random().nextInt(10);

            // Tworzenie obiektu RoomInfo dla danego pokoju
            LobbyInfo roomInfo = new LobbyInfo(roomNumberTmp, playersInRoom);

            // Dodawanie RoomInfo do tablicy rooms
            rooms[i] = roomInfo;
        }


        // Wyświetl przyciski dla każdego pokoju
        for (LobbyInfo room : rooms) {
            if(room == null){
                continue;
            }
            JButton roomButton = new JButton("Pokój " + room.getRoomNumber() + " - Graczy: " + room.getNumPlayers());
            roomButton.addActionListener(e -> joinRoom(room.getRoomNumber()));
            add(roomButton);
        }
        frame = createAndShowGUI();
        scheduler.scheduleAtFixedRate(this::updateRoomPlayersCount, 0, 5, TimeUnit.SECONDS);
    
    }

    // Metoda do aktualizacji ilości graczy w pokojach
    private void updateRoomPlayersCount() {
        SwingUtilities.invokeLater(() -> {
            if (!scheduler.isShutdown()) { // Sprawdź, czy scheduler nie jest zamknięty
                for (Component component : getComponents()) {
                    if (component instanceof JButton) {
                        JButton roomButton = (JButton) component;
                        String buttonText = roomButton.getText();
                        int roomNumber = extractRoomNumber(buttonText);

                        int playersInRoom = getPlayersInLobby(roomNumber);
                        roomButton.setText("Pokój " + roomNumber + " - Graczy: " + playersInRoom);
                    }
                }
            }
        });
    }

    private int extractRoomNumber(String buttonText) {
        // Wyszukaj numer pokoju w tekście przycisku i zwróć go jako int
        String[] parts = buttonText.split(" ");
        return Integer.parseInt(parts[1]);
    }



    public JFrame createAndShowGUI() {
        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JFrame frame = new JFrame("Lobby");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        frame.add(scrollPane);
        frame.setVisible(true);

        // Nie zamykaj gniazda przy zamykaniu okna
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Dodaj kod obsługujący zamknięcie okna
                handleWindowClosing();
            }
        });
        return frame;
    }

    private void handleWindowClosing() {
        // Tutaj możesz dodać kod do obsługi zamknięcia okna
        // Na przykład, zamknij gniazdo lub wykonaj inne niezbędne czynności
        // Następnie zamknij okno
        // Zatrzymaj scheduler przed zamknięciem okna
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        frame.dispose();
        System.exit(0);
    }

    // Zapytanie o ilość dostępnych pokojów i graczy w nich
    private int getAvailableRoomsCount() {
        JSONObject jsonResponse;
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            command = "get_available_rooms_count";

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("command", command);
            jsonRequest.put("username", username);
            System.out.println("request:" + jsonRequest);

            out.println(jsonRequest);

            JsonObjectReader jsonObjectReader = new JsonObjectReader();
            jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);
            return jsonResponse.getInt("rooms_amount");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0; // Obsługa błędu - zwracamy 0
    }

    private int getPlayersInLobby(int roomNumber) {
        JSONObject jsonResponse;
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            command = "get_players_in_lobby";
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("command", command);
            jsonRequest.put("username", username);
            jsonRequest.put("room_number", roomNumber);
            System.out.println("request:" + jsonRequest);

            out.println(jsonRequest);

            JsonObjectReader jsonObjectReader = new JsonObjectReader();
            jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);
            if (jsonResponse.getInt("room_number") == roomNumber) {
                return jsonResponse.getInt("num_players");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0; // Obsługa błędu - zwracamy 0
    }


    private void joinRoom(int roomNumber) {
        // Tutaj możesz wywołać metodę dołączania do wybranego pokoju
        // np. otwarcie okna WisielecClientGUI dla danego pokoju
        if(enterRoom(roomNumber)){
            new WisielecClientGUI(roomNumber, username, socket);
            scheduler.shutdownNow();
            frame.dispose();
            JOptionPane.showMessageDialog(null, "Udało się dołączyć do pokoju.", "Dołączenie do pokoju", JOptionPane.INFORMATION_MESSAGE);
        }
        else{
            frame.dispose();
            new LobbyGUI(username,socket);
            JOptionPane.showMessageDialog(null, "Coś poszło nie tak.", "Dołączenie do pokoju", JOptionPane.ERROR_MESSAGE);
        }

        // Umożliwia przekazanie numeru pokoju do WisielecClientGUI,
        // aby serwer wiedział, który pokój obsłużyć
    }

    private boolean enterRoom(int roomNumber) {
        JSONObject jsonResponse;
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            command = "enter_room";
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("command", command);
            jsonRequest.put("username", username);
            jsonRequest.put("room_number", roomNumber);

            System.out.println("request:" + jsonRequest);

            out.println(jsonRequest);

            JsonObjectReader jsonObjectReader = new JsonObjectReader();
            jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);
            if (jsonResponse.getInt("room_number") == roomNumber) {
                return jsonResponse.getBoolean("success");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // Obsługa błędu - zwracamy false
    }

}




