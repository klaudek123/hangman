package main.java;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class JsonObjectReader {

    public JSONObject readJsonObject(String username, String command, Socket socket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {

            StringBuilder jsonStringBuilder = new StringBuilder();
            char[] buffer = new char[1024];
            int bytesRead;

            while ((bytesRead = reader.read(buffer)) != -1) {
                jsonStringBuilder.append(buffer, 0, bytesRead);

                String jsonString = jsonStringBuilder.toString();
                System.out.println("Received response: " + jsonString);
                if (jsonString.contains("}\n")) {
                    break;
                }
            }

            String jsonString = jsonStringBuilder.toString().replaceAll("\\s", ""); // Usunięcie białych znaków

            System.out.println("Received response: " + jsonString);
            return new JSONObject(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject("error");
    }
}