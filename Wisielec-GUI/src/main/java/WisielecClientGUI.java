package main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WisielecClientGUI extends JPanel {
    private Socket socket;
//    private PrintWriter out;
//    private BufferedReader in;
//
    private JTextArea gameStatusTextArea;
    private JTextField guessInputField;
//    private JButton guessButton;
    private JLabel hangmanLabel;
    private JLabel timerLabel;
    private Timer timer;
    private int secondsLeft;
    private JLabel passwordLabel;
    private String username;
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
        JPanel opponentsPanel = new JPanel(); // Tutaj będzie umieszczony scrollPane
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

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS)); // Używamy BoxLayout z Y_AXIS

        // Inicjalizacja nicku gracza
        JLabel myLabel = new JLabel(username);
        myLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Wyśrodkowanie w osi X


        // Inicjalizacja hangmanLabel dla gracza i ustawienie obrazu
        ImageIcon originalMyImage = new ImageIcon("src\\main\\java\\Images\\hangman2.png");
        Image myResizedImage = originalMyImage.getImage().getScaledInstance(300, 400, Image.SCALE_SMOOTH);
        ImageIcon resizedMyImage = new ImageIcon(myResizedImage);
        JLabel myImageLabel = new JLabel(resizedMyImage);
        myImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Dodanie nicku gracza i jego hangmana do panelu gracza
        myPanel.add(myLabel);
        myPanel.add(Box.createVerticalStrut(10)); // Dodajemy odstęp pomiędzy komponentami
        myPanel.add(myImageLabel);

        // Inicjalizacja i ustawienie scrollPane dla listy wisielców oponentów
        opponentsPanel.setLayout(new BoxLayout(opponentsPanel, BoxLayout.Y_AXIS));

        // Inicjalizacja obrazów hangman i nicków graczy
        try {
            for (int i = 1; i <= 8; i++) {
                // Tworzenie panelu dla nicka gracza i jego hangmana
                JPanel playerPanel = new JPanel(new BorderLayout());

                // Inicjalizacja nicku gracza
                JLabel playerLabel = new JLabel("Gracz " + i);
                playerLabel.setHorizontalAlignment(SwingConstants.CENTER);

                // Inicjalizacja hangmanLabel dla gracza i ustawienie obrazu
                ImageIcon originalHangmanImage = new ImageIcon("src\\main\\java\\Images\\hangman" + i + ".png");
                Image resizedImage = originalHangmanImage.getImage().getScaledInstance(200, 300, Image.SCALE_SMOOTH);
                ImageIcon resizedHangmanImage = new ImageIcon(resizedImage);
                JLabel hangmanImageLabel = new JLabel(resizedHangmanImage);
                hangmanImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

                // Dodanie nicku gracza i jego hangmana do panelu gracza
                playerPanel.add(playerLabel, BorderLayout.NORTH);
                playerPanel.add(hangmanImageLabel, BorderLayout.CENTER);

                // Dodanie panelu gracza do opponentsPanel
                opponentsPanel.add(playerPanel);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

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
        centerPanel.add(myPanel, BorderLayout.WEST);
        centerPanel.add(scrollPane, BorderLayout.EAST);
        bottomPanel.add(guessInputField, BorderLayout.WEST);
        buttonPanel.add(guessButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

// Inicjalizacja głównego okna i ustawienie komponentów
        JFrame frame = new JFrame("Game room");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        frame.setLayout(new BorderLayout());

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
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
