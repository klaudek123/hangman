package main.java;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JsonArrayReader {

    public JSONArray readJsonArray(String username, String command, Socket socket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            StringBuilder jsonArrayStringBuilder = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;

            while ((bytesRead = reader.read(buffer)) != -1) {
                jsonArrayStringBuilder.append(buffer, 0, bytesRead);

                //System.out.println(Arrays.toString(buffer));
                // Sprawdź, czy JSON jest kompletny (zawiera otwierający i zamykający nawias kwadratowy)
                if (jsonArrayStringBuilder.toString().contains("[") && jsonArrayStringBuilder.toString().contains("]")) {
                    break;
                }
            }

//            if (!jsonArrayStringBuilder.toString().contains("[") || !jsonArrayStringBuilder.toString().contains("]")) {
//                // JSON nie jest kompletny, obsłuż ten przypadek (np. rzuć wyjątek)
//                throw new IOException("Incomplete JSON received");
//            }

            System.out.println("Received response: " + jsonArrayStringBuilder.toString());
            return new JSONArray(new JSONTokener(jsonArrayStringBuilder.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONArray(); // Pusta tablica w przypadku błędu
    }
}