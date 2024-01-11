#ifndef GAME_H
#define GAME_H

#pragma once

#include "Player.h"
#include "Room.h"
#include <unordered_map>

class Game
{
private:
    std::unordered_map<int, Room> gameRooms; // Mapa przechowująca pokoje gry, klucz to numer pokoju
    std::string wordToGuess;
    std::unordered_map<std::string, Player> playersMap;
public:
    Game();
    ~Game();
    bool addPlayerToRoom(int roomId, Player* player);
    std::string getWordToGuess();
    std::unordered_map<int, Room>& getGameRooms();
    void addPlayertoMap(const std::string& username, const Player& player);
    Player* getPlayerByUsername(const std::string& username);
    Room* getRoomById(int roomId);
    bool removePlayerFromRoom(int roomId, Player* player);
    void createNewRoom(const Player& player);
    void guessLetter(Player player, char letter);
    void updateGameState();


};

#endif
