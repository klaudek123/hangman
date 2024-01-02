#include "Room.h"

Room::Room()
{

}

Room::~Room()
{

}

void Room::addPlayer(const Player& newPlayer) {
        players.push_back(newPlayer);
}

void Room::removePlayer(Player player) {
    // Logika usuwania gracza z pokoju
    // players.erase(std::remove(players.begin(), players.end(), player), players.end());
}

void Room::startGame() {
    // Logika rozpoczęcia gry w pokoju
}

void Room::endGame() {
    // Logika zakończenia gry w pokoju
}