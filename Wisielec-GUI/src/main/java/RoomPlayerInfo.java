package main.java;

import java.util.ArrayList;
import java.util.List;

public class RoomPlayerInfo {
    private List<PlayerInfo> playerList;

    public RoomPlayerInfo() {
        playerList = new ArrayList<>();
    }

    public void addPlayer(String username, int score, int statusHangman) {
        PlayerInfo playerInfo = new PlayerInfo(username, score, statusHangman);
        playerList.add(playerInfo);
    }

    public void removePlayer(String username) {
        // Iteruj przez listę graczy i usuń gracza o danej nazwie użytkownika
        playerList.removeIf(player -> player.getUsername().equals(username));
    }

    public void clearPlayerList() {
        playerList.clear();
    }

    public List<PlayerInfo> getPlayerList() {
        return playerList;
    }

    public static class PlayerInfo {
        private String username;
        private int score;
        private int hangmanState;

        public PlayerInfo(String username, int score, int hangmanState) {
            this.username = username;
            this.score = score;
            this.hangmanState = hangmanState;
        }

        public String getUsername() {
            return username;
        }

        public int getScore() {
            return score;
        }

        public int getHangmanState() {
            return hangmanState;
        }
    }
    public int getScoreByUsername(String username) {
        for (PlayerInfo player : playerList) {
            if (player.getUsername().equals(username)) {
                return player.getScore();
            }
        }
        // Jeśli nie znaleziono gracza o podanym username, zwróć np. -1 lub wartość domyślną
        return -1;
    }

    public int getHangmanStateByUsername(String username) {
        for (PlayerInfo player : playerList) {
            if (player.getUsername().equals(username)) {
                return player.getHangmanState();
            }
        }
        // Jeśli nie znaleziono gracza o podanym username, zwróć np. -1 lub wartość domyślną
        return -1;
    }
}