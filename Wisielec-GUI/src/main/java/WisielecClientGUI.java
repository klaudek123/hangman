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

public class WisielecClientGUI extends JPanel {
    private Socket socket;
    //    private PrintWriter out;
//    private BufferedReader in;
//
    private JTextArea gameStatusTextArea;
    private final JTextField guessInputField;
    //    private JButton guessButton;
    private JLabel hangmanLabel;
    private final JLabel timerLabel;
    private Timer timer;
    private int secondsLeft;
    private final JLabel passwordLabel;
    private final String username;

    //    public WisielecClientGUI(int roomNumber, String username, Socket socket) {
//        this.socket = socket;
    public WisielecClientGUI(int roomNumber, String username) {
        this.username = username;

        // Inicjalizacja paneli
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // Dodajemy marginesy

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 10, 50, 10)); // Dodajemy marginesy

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10)); // Dodajemy marginesy

        JPanel passwordPanel = new JPanel(new BorderLayout());
        JPanel opponentsPanel = new JPanel();
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Inicjalizacja labela na hasło
        passwordLabel = new JLabel("_ _ _ _ _ _ _ _ _ _ _"); // Na początku wyświetla się zasłonięte hasło
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.PLAIN, 40f)); // Zmiana rozmiaru czcionki
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER); // Wyśrodkowanie tekstu

        // Inicjalizacja labela zegara
        timerLabel = new JLabel("00:00");
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.PLAIN, 20f)); // Zmiana rozmiaru czcionki
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER); // Wyśrodkowanie tekstu
        // Ustawienie czasu odliczania w sekundach (np. 5 minut)
        secondsLeft = 5 * 60; // 5 minut * 60 sekund
        startTimer();

        // Hangman użytkownika
        JPanel myPanel = createMyPanel();
        centerPanel.add(myPanel, BorderLayout.WEST);
        

        // Inicjalizacja obrazów hangman i nicków graczy(oponentow)
        opponentsPanel = createOpponentsPanel(8);
        JScrollPane scrollPane = new JScrollPane(opponentsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Inicjalizacja pozostałych komponentów (guessInputField, guzik)
        guessInputField = new JTextField(20);
        JButton guessButton = new JButton("Zgadnij");

        // Dodanie ActionListenera dla guzika
        guessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String guess = guessInputField.getText().trim();
                //out.println(guess); // Wysyłanie zgadywanej litery do serwera
                guessInputField.setText(""); // Czyszczenie pola po wysłaniu litery
                guessInputField.requestFocus(); // Ustawienie fokusu na polu tekstowym po wysłaniu litery
                System.out.println("Kliknięto guzik!");
            }
        });

        // Dodawanie komponentów do odpowiednich paneli
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(timerLabel, BorderLayout.EAST);
        topPanel.add(passwordPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.EAST);
        bottomPanel.add(guessInputField, BorderLayout.WEST);
        buttonPanel.add(guessButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Inicjalizacja głównego okna i ustawienie komponentów
        JFrame frame = new JFrame("Game room nr "+roomNumber);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        frame.setLayout(new BorderLayout());

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel createMyPanel() {
//        JPanel myPanel = new JPanel();
//        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
//
//        JLabel myLabel = new JLabel(username);
//        myLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//
//        JLabel myImageLabel = createHangmanLabel("src\\main\\java\\Images\\hangman2.png");
//        myPanel.add(myLabel);
//        myPanel.add(Box.createVerticalStrut(10));
//        myPanel.add(myImageLabel);
//
//        return myPanel;


        //#TODO odpytanie serwera ile username ma pkt i jak wygląda jego wisielec
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));

        JLabel myLabel = new JLabel(username);
        myLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Pobranie punktów użytkownika
        int userPoints = getUserPoints(username);

        // Pobranie wyglądu wisielca użytkownika
        String userHangmanImage = getUserHangmanAppearance(username);

        // Stworzenie etykiety z punktami użytkownika
        JLabel pointsLabel = new JLabel("Punkty: " + userPoints);
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stworzenie etykiety z wyglądem wisielca użytkownika
        JLabel myImageLabel = createHangmanLabel(userHangmanImage);
        myPanel.add(myLabel);
        myPanel.add(pointsLabel);
        myPanel.add(Box.createVerticalStrut(10));
        myPanel.add(myImageLabel);

        return myPanel;
    }
    // Metoda do pobrania punktów użytkownika
    private int getUserPoints(String username) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("command", "get_user_points");
            jsonRequest.put("username", username);

            out.println(jsonRequest.toString());

            String response = in.readLine();
            JSONObject jsonResponse = new JSONObject(response);

            return jsonResponse.getInt("points");
        } catch (IOException e) {
            e.printStackTrace();
            return 0; // Obsługa błędu - zwracamy 0
        }
    }

    // Metoda do pobrania wyglądu wisielca użytkownika
    private String getUserHangmanAppearance(String username) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("command", "get_user_hangman_appearance");
            jsonRequest.put("username", username);

            out.println(jsonRequest.toString());

            String response = in.readLine();
            JSONObject jsonResponse = new JSONObject(response);

            return jsonResponse.getString("hangmanImage");
        } catch (IOException e) {
            e.printStackTrace();
            //#TODO hangman0.png
            return "src\\main\\java\\Images\\hangman0.png"; // Obsługa błędu - zwracamy obraz domyślny
        }
    }


    private JPanel createOpponentsPanel(int k) {

        //#TODO  zapytanie serwera o ilość graczy wraz ze stanem ich pkt w pokoju

        JPanel opponentsPanel = new JPanel();
        opponentsPanel.setLayout(new BoxLayout(opponentsPanel, BoxLayout.Y_AXIS));

        for (int i = 1; i <= k; i++) {
            JPanel playerPanel = new JPanel(new BorderLayout());

            JLabel playerLabel = new JLabel("Gracz " + i);
            playerLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel hangmanImageLabel = createHangmanLabel("src\\main\\java\\Images\\hangman" + i + ".png");
            hangmanImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            playerPanel.add(playerLabel, BorderLayout.NORTH);
            playerPanel.add(hangmanImageLabel, BorderLayout.CENTER);

            opponentsPanel.add(playerPanel);
        }
        return opponentsPanel;
    }

    private JLabel createHangmanLabel(String imagePath) {
        JLabel hangmanLabel = new JLabel();
        try {
            ImageIcon originalImage = new ImageIcon(imagePath);
            Image resizedImage = originalImage.getImage().getScaledInstance(200, 300, Image.SCALE_SMOOTH);
            ImageIcon resizedHangmanImage = new ImageIcon(resizedImage);
            hangmanLabel.setIcon(resizedHangmanImage);
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
                    // Tutaj można dodać akcje wykonywane po zakończeniu czasu
                    System.out.println("Czas się skończył!");
                }
            }
        });
        timer.start();
    }

// Gdzieś w logice gry, gdy liczba błędnych prób ulegnie zmianie, możesz wywołać:
// drawHangman z aktualną liczbą błędnych prób
// Na przykład:

    // Metoda do aktualizacji interfejsu na podstawie otrzymanej wiadomości od serwera
    private void updateGameStatus(String statusMessage) {
        SwingUtilities.invokeLater(() -> {
            gameStatusTextArea.append(statusMessage + "\n");
            // Możesz dodać logikę do wyświetlania wisielca, odsłaniania liter, itp.
        });
    }

    // Metoda do obsługi zakończenia gry (jeśli został jeden gracz w pokoju)
    private void endGame() {
        // Wyświetl komunikat o zakończeniu gry
    }

    // Metoda do pobierania słowa z serwera
    private String getWordFromServer() {
        // Pobierz słowo z serwera
        return "Słowo z serwera";
    }

    // Metoda do rysowania stanu Wisielca
    public void drawHangman(int incorrectGuessCount) {
        // Utworzenie ścieżki do pliku
        String imagePath = "Images/hangman" + incorrectGuessCount + ".png";

        // Wczytanie obrazka i przypisanie go do hangmanLabel
        ImageIcon hangmanImage = new ImageIcon(imagePath);
        hangmanLabel.setIcon(hangmanImage);
    }

    // Metoda do odsłaniania liter w haśle
    private void revealLetterInWord(char guessedLetter) {
        // Odsłoń literę w haśle
    }

    // Metoda do aktualizowania rankingu graczy w pokoju
    private void updatePlayerRanking(List playerList) {
        // Aktualizuj ranking graczy w pokoju
    }

    // Metoda do wysyłania zgadywanej litery do serwera
    private void sendGuessToServer(String guess) {
        // Wysyłanie zgadywanej litery do serwera
    }

    // Metoda do aktualizacji wyświetlanego hasła
    private void updatePasswordLabel(String password) {
        passwordLabel.setText(password); // Ustawienie aktualnego hasła w labelu
    }

    private void updateTimer() {
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft % 60;

        String timeString = String.format("%02d:%02d", minutes, seconds);
        timerLabel.setText(timeString);

        if (secondsLeft > 0) {
            secondsLeft--;
        } else {
            timer.stop(); // Zatrzymanie odliczania, gdy czas minie
            // Tutaj możesz dodać logikę zakończenia gry po upływie czasu
        }
    }
}
