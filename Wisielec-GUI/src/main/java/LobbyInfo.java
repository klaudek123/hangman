package main.java;

// Klasa reprezentująca informacje o pokoju
class LobbyInfo {
    private int roomNumber;
    private int numPlayers;

    public LobbyInfo(int roomNumber, int numPlayers) {
        this.roomNumber = roomNumber;
        this.numPlayers = numPlayers;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getNumPlayers() {
        return numPlayers;
    }
}
