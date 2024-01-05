package main.java;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
//        try {
            //Socket socket = new Socket("localhost", 12345); // Połączenie z serwerem na porcie 12345

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //new LoginGUI(socket); // Uruchomienie interfejsu graficznego lobby
                    new LoginGUI(); // Uruchomienie interfejsu graficznego lobby
                }
            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}