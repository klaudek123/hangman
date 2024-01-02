#ifndef ROOM_H
#define ROOM_H

#pragma once

#include "Player.h"
#include <vector>

class Room
{
private:
    std::vector<Player> players;
    // Dodaj pola związane z grą, np. słowo, stan wisielca itp.
public:
    Room();
    ~Room();
    void addPlayer(const Player& newPlayer);
    void removePlayer(Player player);
    void startGame();
    void endGame();
};

#endif