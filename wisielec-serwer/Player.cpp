#include "Player.h"

Player::Player()
{

}

Player::~Player()
{

}

Player::Player(const std::string &playerName)
{
    allPlayerNames.insert(playerName);
}

std::string Player::getName()
{
    return name;
}

int Player::getScore() {
    return score;
}

void Player::updateScore(int points) {
    score += points;
}

int Player::getHangmanState() {
    return hangmanState;
}

void Player::updateHangmanState(int newState) {
    hangmanState = newState;
}

bool Player::addNewPlayer(const std::string &playerName)
{
   if (allPlayerNames.count(playerName) == 0) {
            allPlayerNames.insert(playerName);
            return true;
        }
        return false;
}

bool Player::doesPlayerExist(const std::string &playerName)
{
    return allPlayerNames.count(playerName) > 0;
}
