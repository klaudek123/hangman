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
public:
    Game();
    ~Game();
    void addPlayerToRoom(int roomId, const Player& newPlayer);
    std::string getWordToGuess();
    void setWordToGuess(std::string word);
    void guessLetter(Player player, char letter);
    void updateGameState();

};

#endif