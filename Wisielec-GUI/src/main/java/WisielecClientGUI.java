package main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WisielecClientGUI extends JFrame {
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea gameStatusTextArea;
    private JTextField guessInputField;
    private JButton guessButton;
    private JLabel usernameLabel;
    private Socket socket;
//    public WisielecClientGUI(int roomNumber, String username, Socket socket) {
//        this.socket = socket;
    public WisielecClientGUI(int roomNumber, String username) {
        setTitle("Wisielec - Gra");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);

        // Tworzenie interfejsu użytkownika
        gameStatusTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(gameStatusTextArea);
        guessInputField = new JTextField();
        guessInputField.setPreferredSize(new Dimension(200, 30)); // Szerokość 200 pikseli, wysokość 30 pikseli

        guessButton = new JButton("Zgadnij");
        Dimension buttonSize = new Dimension(100, 100);
        guessButton.setPreferredSize(buttonSize);
        guessButton.setMinimumSize(buttonSize);
        guessButton.setMaximumSize(buttonSize);

        guessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String guess = guessInputField.getText().trim();
                //out.println(guess); // Wysyłanie zgadywanej litery do serwera
                guessInputField.setText(""); // Czyszczenie pola po wysłaniu litery
                guessInputField.requestFocus(); // Ustawienie fokusu na polu tekstowym po wysłaniu litery
            }
        });

        guessInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String guess = guessInputField.getText().trim();
                out.println(guess); // Wysyłanie zgadywanej litery do serwera
                guessInputField.setText(""); // Czyszczenie pola po wysłaniu litery
                guessInputField.requestFocus(); // Ustawienie fokusu na polu tekstowym po wysłaniu litery
                System.out.println("Kliknięto enter!");
            }
        });

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(guessButton, gbc);


        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(guessInputField, BorderLayout.SOUTH);
        getContentPane().add(buttonPanel, BorderLayout.EAST);

        setVisible(true);

        // Inicjalizacja połączenia sieciowego
//        try {
//            out = new PrintWriter(socket.getOutputStream(), true);
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            // Wątek do odbierania aktualizacji stanu gry od serwera
//            Thread receivingThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    String message;
//                    try {
//                        while ((message = in.readLine()) != null) {
//                            updateGameStatus(message); // Aktualizacja stanu gry na podstawie wiadomości od serwera
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            receivingThread.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void updateGameStatus(String statusMessage) {
        // Metoda do aktualizacji interfejsu na podstawie otrzymanej wiadomości od serwera
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gameStatusTextArea.append(statusMessage + "\n");
                // Możesz tu dodać logikę do wyświetlania wisielca, odsłaniania liter, itp.
            }
        });
    }
}
