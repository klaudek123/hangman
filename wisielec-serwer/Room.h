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
    std::vector<Player> players;
    // Dodaj pola związane z grą, np. słowo, stan wisielca itp.
public:
    Room();
    ~Room();
    void addPlayer(const Player& player);
    void removePlayer(const Player& playertoRemove);
    int getNumPlayers() const;
    std::vector<std::string> getPlayers() const;

    void startGame();
    void endGame();
};

#endif
