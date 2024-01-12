#ifndef PLAYER_H
#define PLAYER_H

#pragma once

#include <string>
#include <unordered_set>
#include <chrono>

class Player
{
private:
    std::string name;
    int score;
    int hangmanState;
    int socket;
    int roomId;
    std::chrono::steady_clock::time_point lastrequest;
    // Dodaj dodatkowe pola, np. punktację, stan wisielca itp.
public:
    Player(const std::string& playerName, int playerSocket);
    ~Player();
    std::string getName() const;
    int getScore()const;
    int getRoomId();
    void resetScore();
    void updateTime();
    std::chrono::steady_clock::time_point getTime() const;
    void setRoomId(int roomId);
    int getSocket() const;
    void updateScore(int points);
    void resetHangmanState();
    int getHangmanState() const;
    void updateHangmanState();
    static bool addNewPlayer(const std::string& playerName);
    static bool doesPlayerExist(const std::string& playerName);
};

#endif
