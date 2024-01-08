#ifndef PLAYER_H
#define PLAYER_H

#pragma once

#include <string>
#include <unordered_set>

class Player
{
private:
    std::string name;
    int score;
    int hangmanState;
    int socket;
    static std::unordered_set<std::string> allPlayerNames; // Przechowuje unikalne nazwy graczy
    // Dodaj dodatkowe pola, np. punktację, stan wisielca itp.
public:
    Player(const std::string& playerName, int playerSocket);
    ~Player();
    std::string getName() const;
    int getScore();
    void updateScore(int points);
    int getHangmanState();
    void updateHangmanState(int newState);
    static bool addNewPlayer(const std::string& playerName);
    static bool doesPlayerExist(const std::string& playerName);
};

#endif
