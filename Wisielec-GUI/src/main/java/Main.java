package main.java;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("192.168.0.12", 8080); // Połączenie z serwerem na porcie 12345
            System.out.println("tak, dziala połączenie socketem!");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new LoginGUI(socket); // Uruchomienie interfejsu graficznego lobby
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}