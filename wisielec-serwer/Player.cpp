#include "Player.h"

std::unordered_set<std::string> Player::allPlayerNames;

Player::Player(const std::string& playerName, int playerSocket) : name(playerName), score(0), hangmanState(0), socket(playerSocket)
{

}

Player::~Player()
{

}

std::string Player::getName() const
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
