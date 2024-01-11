#ifndef ROOM_H
#define ROOM_H

#pragma once

#include "Player.h"
#include <vector>
#include <iostream>

class Room
{
private:
    int roomId;
    std::string wordtoguess;
    std::vector<Player> players;
    std::unordered_set<char> guessedLetters;
    // Dodaj pola związane z grą, np. słowo, stan wisielca itp.
public:
    Room();
    ~Room();
    void addPlayer(const Player& player);
    std::string getFirstPlayer() const;
    bool isLetterGuessed(char letter) const;
    void addGuessedLetter(char letter);
    void removePlayer(const Player& playertoRemove);
    int getNumPlayers() const;
    std::unordered_set<char> getGuessed();
    void setWord(std::string word);
    std::string getWord();
    const std::vector<Player>& getPlayers() const;

    void startGame();
    void endGame();
};

#endif
