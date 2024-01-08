#include "Room.h"
#include "Player.h"
#include <algorithm>

Room::Room(){};
Room::~Room() {};

void Room::addPlayer(const Player& newPlayer) {
    players.push_back(newPlayer);
}

int Room::getNumPlayers()const {
    return players.size();
}
void Room::removePlayer(const Player& playertoRemove) {
    players.erase(std::remove_if(players.begin(), players.end(),
                                 [&](const Player& player)
    {return &player == &playertoRemove; }) ,
    players.end());
}


void Room::startGame() {

}
/*std::vector<std::string>Room::getPlayers() const {
    std::vector<std::string> playerNames;
    for(const auto& player : players){
        playerNames.push_back(player.getName());
    }

}*/
void Room::endGame() {
    // Logika zakończenia gry w pokoju
}
