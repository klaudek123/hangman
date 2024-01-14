package main.java;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Array;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class WisielecClientGUI extends JPanel {
    private Socket socket;
    private final JFrame frame;
    private PrintWriter out;
    private JTextArea gameStatusTextArea;
    private final JTextField guessInputField;

    private JLabel hangmanLabel;
    private final JLabel timerLabel;
    private Timer timer;
    private int secondsLeft;
    private final JLabel passwordLabel;
    private JLabel guessedLettersLabel;
    private final String username;
    private String command;
    private RoomPlayerInfo roomPlayerInfo;
    private JPanel myPanel;
    JPanel opponentsPanel;
    private int roomNumber;
    private String word = "";
    //private Time wordTime;
    private final Timer refreshTimer;
    private final int refreshInterval = 2000; // Okres odświeżania w milisekundach


    public WisielecClientGUI(int roomNumber, String username, Socket socket) {
        this.socket = socket;
        this.username = username;
        this.roomNumber = roomNumber;
        this.roomPlayerInfo = new RoomPlayerInfo();

        startGame();

        updateRoomPlayerInfo(roomPlayerInfo);

        // Inicjalizacja paneli
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // Dodajemy marginesy

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 10, 50, 10)); // Dodajemy marginesy

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10)); // Dodajemy marginesy

        // Inicjalizacja panela na hasło
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        // Inicjalizacja głównego panelu
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Inicjalizacja guzika "Prawo do góry"
        JButton scrollToTopButton = new JButton("Start Game!");
        scrollToTopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });


        // Inicjalizacja labela na hasło
        passwordLabel = new JLabel(generateMaskedWord(word)); // Na początku wyświetla się zasłonięte hasło
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.PLAIN, 40f)); // Zmiana rozmiaru czcionki
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER); // Wyśrodkowanie tekstu

        // Inicjalizacja labela na wybrane litery
        guessedLettersLabel = new JLabel("Odgadnięte litery: ");
        guessedLettersLabel.setFont(guessedLettersLabel.getFont().deriveFont(Font.PLAIN, 16f)); // Zmiana rozmiaru czcionki
        guessedLettersLabel.setHorizontalAlignment(SwingConstants.CENTER); // Wyśrodkowanie tekstu



        // Inicjalizacja labela zegara
        timerLabel = new JLabel("00:00");
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.PLAIN, 20f)); // Zmiana rozmiaru czcionki
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER); // Wyśrodkowanie tekstu

//        // Inicjalizacja przykładowego czasu, na przykład 5 minut od teraz
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.MINUTE, 5);
//        this.wordTime = new Time(calendar.getTimeInMillis());

        // Hangman użytkownika
        myPanel = createMyPanel();
        centerPanel.add(myPanel, BorderLayout.WEST);
        

        // Inicjalizacja obrazów hangman i nicków graczy(oponentow)
        opponentsPanel = createOpponentsPanel();
        JScrollPane scrollPane = new JScrollPane(opponentsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel inputLeavePanel = new JPanel(new BorderLayout());

        // Inicjalizacja pozostałych komponentów (guessInputField, guzik)
        guessInputField = new JTextField(20);
        JButton guessButton = new JButton("Zgadnij");

        // Dodanie ActionListenera dla guzika
        guessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String guess = guessInputField.getText().trim();
                sendLetter(guess); // Wysyłanie zgadywanej litery do serwera
                guessInputField.setText(""); // Czyszczenie pola po wysłaniu litery
                guessInputField.requestFocus(); // Ustawienie fokusu na polu tekstowym po wysłaniu litery
                System.out.println("Kliknięto guzik!");
            }
        });

        JButton leaveButton = new JButton("Opuść pokój");

        // Inicjalizacja komponentów do odstępu między guzikami
        Component horizontalStrut = Box.createHorizontalStrut(100); // Możesz dostosować szerokość odstępu według potrzeb
        inputLeavePanel.add(guessButton, BorderLayout.WEST);
        inputLeavePanel.add(horizontalStrut);
        inputLeavePanel.add(leaveButton, BorderLayout.EAST);

        // Dodanie ActionListenera dla guzika
        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leaveRoom(username);
                System.out.println("Opuszczono grę!");
            }
        });

        // Dodawanie komponentów do odpowiednich paneli
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(timerLabel, BorderLayout.EAST);

        // Dodawanie paneli do głównego panelu
        mainPanel.add(passwordPanel);
        mainPanel.add(guessedLettersLabel);
        topPanel.add(scrollToTopButton, BorderLayout.WEST);
        topPanel.add(mainPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.EAST);
        bottomPanel.add(guessInputField, BorderLayout.WEST);
        buttonPanel.add(inputLeavePanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Inicjalizacja głównego okna i ustawienie komponentów
        frame = new JFrame("Game room nr "+roomNumber);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        frame.setLayout(new BorderLayout());

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Utwórz timer do okresowego odświeżania stanu gry
        refreshTimer = new Timer(refreshInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Wywołaj metody odświeżające hangmanów, punkty i inne informacje z serwera
                updateRoomPlayerInfo(roomPlayerInfo);
                refreshOpponentsPanel();
                refreshUserPanel();
                // Dodaj inne metody odświeżające, jeśli są potrzebne
            }
        });
        if(refreshTimer != null){
            refreshTimer.start(); // Rozpocznij odświeżanie
        }


        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Dodaj kod obsługujący zamknięcie okna
                handleWindowClosing();
            }
        });

        frame.setVisible(true);
    }

    private void startGame() {
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            // Tworzymy żądanie do serwera
            JSONObject jsonRequest = new JSONObject();
            command = "start_game";
            jsonRequest.put("command", command);
            jsonRequest.put("username", username);

            System.out.println("request:" + jsonRequest);
            // Wysyłamy żądanie na serwer
            out.println(jsonRequest.toString());

            // Odczytujemy odpowiedź z serwera
            JsonObjectReader jsonObjectReader = new JsonObjectReader();
            JSONObject jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);
            System.out.println(jsonResponse);
//            if(!jsonResponse.getBoolean("success")){
//                new LobbyGUI(username, socket);
//                if(refreshTimer != null){
//                    refreshTimer.stop(); // Rozpocznij odświeżanie
//                }
//                if(frame !=null){
//                    frame.dispose();
//                }
//                JOptionPane.showMessageDialog(null, "Nie udało się rozpocząć gry!", "Stan gry", JOptionPane.INFORMATION_MESSAGE);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWindowClosing() {
        // Tutaj możesz dodać kod do obsługi zamknięcia okna
        // Na przykład, zamknij gniazdo lub wykonaj inne niezbędne czynności
        // Następnie zamknij okno
        // Zatrzymaj scheduler przed zamknięciem okna
        refreshTimer.stop();
        leaveRoom(username);
        frame.dispose();
    }

    private void leaveRoom(String username) {
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            // Tworzymy żądanie do serwera
            JSONObject jsonRequest = new JSONObject();
            command = "leave_room";
            jsonRequest.put("command", command);
            jsonRequest.put("username", username);

            System.out.println("request:" + jsonRequest);
            // Wysyłamy żądanie na serwer
            out.println(jsonRequest.toString());

            // Odczytujemy odpowiedź z serwera
            JsonObjectReader jsonObjectReader = new JsonObjectReader();
            JSONObject jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);
            System.out.println(jsonResponse);
            if(jsonResponse.getBoolean("success")){
                new LobbyGUI(username, socket);
                if(refreshTimer != null){
                    refreshTimer.stop(); // Rozpocznij odświeżanie
                }
                frame.dispose();
                JOptionPane.showMessageDialog(null, "Opuszczono gre!", "Stan gry", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                JOptionPane.showMessageDialog(null, "Nie udało się opuścić gre!", "Stan gry", JOptionPane.INFORMATION_MESSAGE);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshUserPanel() {
        SwingUtilities.invokeLater(() -> {
            // Aktualizacja panelu użytkownika (myPanel)
            JLabel pointsLabel = (JLabel) myPanel.getComponent(1); // Pobierz etykietę punktów z myPanel
            int userPoints = roomPlayerInfo.getScoreByUsername(username);
            pointsLabel.setText("Punkty: " + userPoints);

            // Dodatkowo, jeśli masz obraz hangmana użytkownika, możesz go również zaktualizować
            JLabel myImageLabel = (JLabel) myPanel.getComponent(3); // Pobierz etykietę z obrazem hangmana z myPanel
            int userHangmanState = roomPlayerInfo.getHangmanStateByUsername(username);
            myImageLabel.setIcon(new ImageIcon("src\\main\\java\\Images\\hangman" + userHangmanState + ".png"));
        });
    }

    private void refreshOpponentsPanel() {
        SwingUtilities.invokeLater(() -> {
            // Wyczyść istniejące panle
            opponentsPanel.removeAll();

            List<RoomPlayerInfo.PlayerInfo> playerList = roomPlayerInfo.getPlayerList();

            for (RoomPlayerInfo.PlayerInfo player : playerList) {
                // Pomijaj użytkownika klienta
                if (!player.getUsername().equals(username)) {
                    JPanel playerPanel = new JPanel(new BorderLayout());

                    JLabel playerLabel = new JLabel("Gracz: " + player.getUsername());
                    playerLabel.setHorizontalAlignment(SwingConstants.LEFT);

                    // Pobranie punktów użytkownika
                    int userPoints = player.getScore();

                    // Stworzenie etykiety z punktami użytkownika
                    JLabel pointsLabel = new JLabel("Punkty: " + userPoints);
                    pointsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                    // Panel do umieszczenia etykiet z nazwą gracza i punktami gracza w jednym wierszu
                    JPanel namePointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    namePointsPanel.add(playerLabel);
                    namePointsPanel.add(Box.createHorizontalGlue());  // Dodaj elastyczną przestrzeń
                    namePointsPanel.add(pointsLabel);

                    JLabel hangmanImageLabel = createHangmanLabel("src\\main\\java\\Images\\hangman" + player.getHangmanState() + ".png");
                    hangmanImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

                    playerPanel.add(namePointsPanel, BorderLayout.NORTH);  // Dodaj namePointsPanel
                    playerPanel.add(hangmanImageLabel, BorderLayout.CENTER);

                    opponentsPanel.add(playerPanel);
                }
            }

            // Przeorganizuj layout i odśwież panel
            opponentsPanel.revalidate();
            opponentsPanel.repaint();
        });
    }


    private void sendLetter(String guess) {
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            // Tworzymy żądanie do serwera
            JSONObject jsonRequest = new JSONObject();
            command = "guessed_letter";
            jsonRequest.put("command", command);
            jsonRequest.put("username", username);
            jsonRequest.put("letter", guess);

            System.out.println("request:" + jsonRequest);
            // Wysyłamy żądanie na serwer
            out.println(jsonRequest.toString());

            // Odczytujemy odpowiedź z serwera
            JsonObjectReader jsonObjectReader = new JsonObjectReader();
            JSONObject jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);
            System.out.println(jsonResponse);
            //handleGuessedLetterResponse(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGuessedLetterResponse(JSONObject jsonResponse) {
        try {
            String word = jsonResponse.getString("word");
            String guessedLetters = jsonResponse.getString("guessed_letters");

            SwingUtilities.invokeLater(() -> {
                // Odsłoń litery w haśle
                revealGuessedLetters(word, guessedLetters);

                // Wyświetl odgadnięte litery (zarówno poprawne, jak i niepoprawne)
                displayGuessedLetters(guessedLetters);
            });
        } catch (JSONException e) {
            e.printStackTrace();
            // Obsłuż błąd parsowania JSON lub braku wymaganych pól w odpowiedzi
        }
    }

    private void revealGuessedLetters(String word, String guessedLetters) {
        StringBuilder revealedWord = new StringBuilder();

        // Iteruj przez litery w haśle
        for (int i = 0; i < word.length(); i++) {
            char currentChar = word.charAt(i);

            // Sprawdź, czy litera jest odgadnięta
            if (guessedLetters.contains(String.valueOf(currentChar))) {
                // Litera jest odgadnięta - dodaj ją do odsłoniętego hasła
                revealedWord.append(currentChar);
            } else {
                // Litera nie jest odgadnięta - dodaj znak zastępczy (np. "_")
                revealedWord.append("_");
            }
        }
        String spacedRevealedWord = String.join(" ", revealedWord.toString().split(""));

        // Ustaw odsłonięte hasło na etykiecie
        // Załóżmy, że masz etykietę o nazwie passwordLabel
        passwordLabel.setText(spacedRevealedWord);
    }

    private void displayGuessedLetters(String guessedLetters) {
        // Dodaj spację między każdą literką w tekście
        String spacedLetters = String.join(" ", guessedLetters.split(""));

        // Przypisz tekst do guessedLettersLabel
        guessedLettersLabel.setText("Odgadnięte litery: " + spacedLetters);
    }

    // Metoda do generowania zasłoniętego słowa z spacjami
    private String generateMaskedWord(String word) {
        StringBuilder maskedWord = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char currentChar = word.charAt(i);
            if (Character.isLetter(currentChar)) {
                maskedWord.append("_ ");
            } else {
                maskedWord.append(currentChar);
            }
        }
        return maskedWord.toString().trim(); // Usunięcie ewentualnej spacji na końcu
    }

    private void updateRoomPlayerInfo(RoomPlayerInfo roomPlayerInfo) {
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            // Tworzymy żądanie do serwera
            JSONObject jsonRequest = new JSONObject();
            command = "get_room_player_info";
            jsonRequest.put("command", command);
            jsonRequest.put("username", username);
            jsonRequest.put("room_number", roomNumber);

            System.out.println("request:" + jsonRequest);
            // Wysyłamy żądanie na serwer
            out.println(jsonRequest.toString());

            // Odczytujemy odpowiedź z serwera
            JsonObjectReader jsonObjectReader = new JsonObjectReader();
            JSONObject jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);



            // Sprawdź, czy istnieje klucz "players_info" w odpowiedzi
            if (jsonResponse.has("info")) {

                // Czyszczenie istniejącej listy
                roomPlayerInfo.clearPlayerList();

                // Pobierz tablicę obiektów "players_info"
                JSONArray playersInfoArray = jsonResponse.getJSONArray("info");

                // Iteracja po obiektach w array
                for (int i = 0; i < playersInfoArray.length(); i++) {
                    JSONObject playerInfoObject = playersInfoArray.getJSONObject(i);
                    String playerName = playerInfoObject.getString("username");
                    int playerScore = playerInfoObject.getInt("score");
                    int playerHangmanState = playerInfoObject.getInt("hangman_state");

                    // Dodawanie informacji o graczu do roomPlayerInfo
                    roomPlayerInfo.addPlayer(playerName, playerScore, playerHangmanState);
                }
            } else {
                System.out.println("Brak klucza 'players_info' w odpowiedzi z serwera");
            }

            if(jsonResponse.has("time") && !Objects.equals(word, jsonResponse.getString("word"))){
                setupTimerFromServer(jsonResponse.getString("time"));
                word = jsonResponse.getString("word");
            }
            if(jsonResponse.has("guessed_letters") && jsonResponse.has("word")){
                handleGuessedLetterResponse(jsonResponse);
            }


            System.out.println(roomPlayerInfo.getPlayerList());

            // Możesz wywołać dodatkowe metody do zaktualizowania interfejsu
            // np. updatePlayerLabels(roomPlayerInfo);

        } catch (IOException e) {
            e.printStackTrace();
            // Obsługa błędu - np. wyświetlenie komunikatu o błędzie
        }
    }

    private JPanel createMyPanel() {
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));

        JLabel myLabel = new JLabel(username);
        myLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Pobranie punktów użytkownika
        int userPoints = roomPlayerInfo.getScoreByUsername(username);
        // Pobranie wyglądu wisielca użytkownika
        int userHangmanState = roomPlayerInfo.getHangmanStateByUsername(username);

        // Stworzenie etykiety z punktami użytkownika
        JLabel pointsLabel = new JLabel("Punkty: " + userPoints);
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stworzenie etykiety z wyglądem wisielca użytkownika
        JLabel myImageLabel = createHangmanLabel("src\\main\\java\\Images\\hangman" + userHangmanState + ".png");
        myImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        myPanel.add(myLabel);
        myPanel.add(pointsLabel);
        myPanel.add(Box.createVerticalStrut(10));
        myPanel.add(myImageLabel);

        return myPanel;
    }

    private JPanel createOpponentsPanel() {
        JPanel opponentsPanel = new JPanel();
        opponentsPanel.setLayout(new BoxLayout(opponentsPanel, BoxLayout.Y_AXIS));

        List<RoomPlayerInfo.PlayerInfo> playerList = roomPlayerInfo.getPlayerList();

        for (RoomPlayerInfo.PlayerInfo player : playerList) {
            // Pomijaj użytkownika klienta
            if (!player.getUsername().equals(username)) {
                JPanel playerPanel = new JPanel(new BorderLayout());

                JLabel playerLabel = new JLabel("Gracz: " + player.getUsername());
                playerLabel.setHorizontalAlignment(SwingConstants.LEFT);

                // Pobranie punktów użytkownika
                int userPoints = player.getScore();

                // Stworzenie etykiety z punktami użytkownika
                JLabel pointsLabel = new JLabel("Punkty: " + userPoints);
                pointsLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                // Panel do umieszczenia etykiet z nazwą gracza i punktami gracza w jednym wierszu
                JPanel namePointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                namePointsPanel.add(playerLabel);
                namePointsPanel.add(Box.createHorizontalGlue());  // Dodaj elastyczną przestrzeń
                namePointsPanel.add(pointsLabel);

                JLabel hangmanImageLabel = createHangmanLabel("src\\main\\java\\Images\\hangman" + player.getHangmanState() + ".png");
                hangmanImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

                playerPanel.add(namePointsPanel, BorderLayout.NORTH);  // Dodaj namePointsPanel
                playerPanel.add(hangmanImageLabel, BorderLayout.CENTER);

                opponentsPanel.add(playerPanel);
            }
        }
        return opponentsPanel;
    }

    private JLabel createHangmanLabel(String imagePath) {
        JLabel hangmanLabel = new JLabel();
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            Image scaledImage = originalImage.getScaledInstance(200, 300, Image.SCALE_SMOOTH);
            ImageIcon scaledHangmanImage = new ImageIcon(scaledImage);
            hangmanLabel.setIcon(scaledHangmanImage);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return hangmanLabel;
    }

    private void startTimer() {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secondsLeft > 0) {
                    int minutes = secondsLeft / 60;
                    int seconds = secondsLeft % 60;
                    timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                    secondsLeft--;
                } else {
                    ((Timer) e.getSource()).stop(); // Zatrzymaj timer gdy upłynie czas
                    timeEnd();
                    // Tutaj można dodać akcje wykonywane po zakończeniu czasu
                    System.out.println("Czas się skończył!");
                }
            }
        });
        timer.start();
    }

    private void timeEnd() {
        try {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (SocketException se) {
                if (socket.isClosed()) {
                    socket = new Socket("192.168.0.12", 8080);
                    out = new PrintWriter(socket.getOutputStream(), true);
                }
            }
            // Tworzymy żądanie do serwera
            JSONObject jsonRequest = new JSONObject();
            command = "time_end";
            jsonRequest.put("command", command);
            jsonRequest.put("username", username);

            System.out.println("request:" + jsonRequest);
            // Wysyłamy żądanie na serwer
            out.println(jsonRequest.toString());

            // Odczytujemy odpowiedź z serwera
            JsonObjectReader jsonObjectReader = new JsonObjectReader();
            JSONObject jsonResponse = jsonObjectReader.readJsonObject(username, command, socket);
            System.out.println(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTimerFromServer(String timeFromServer) {
        // Pobierz czas od serwera i przekształć go na obiekt LocalTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime serverTime = LocalTime.parse(timeFromServer, formatter);

        // Pobierz obecny czas
        LocalTime currentTime = LocalTime.now();

        System.out.println("Serwer time: " + serverTime);
        System.out.println("Local time: " + currentTime);

        // Oblicz różnicę czasu w sekundach
        long timeDifferenceInSeconds = serverTime.until(currentTime, ChronoUnit.SECONDS);
        System.out.println("Różnica czasu: " + timeDifferenceInSeconds);

        // Ustawienie czasu odliczania w sekundach (pomniejszonego o 5 minut)
        secondsLeft = (int) (5 * 60 - timeDifferenceInSeconds);

        // Uruchom timer
        startTimer();
    }
}
