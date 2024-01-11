#include "Player.h"

Player::Player(const std::string& playerName, int playerSocket) : name(playerName), score(0), hangmanState(0), socket(playerSocket), roomId(0)
{

}

Player::~Player()
{

}

std::string Player::getName() const
{
    return name;
}
void Player::setRoomId(int roomID) {
    roomId = roomID;
}
int Player::getSocket() const {
    return socket;
}
int Player::getScore() {
    return score;
}

void Player::updateScore() {
    score += 1;
}

int Player::getHangmanState() {
    return hangmanState;
}

void Player::updateHangmanState() {
    hangmanState += 1;
}
void Player::resetHangmanState(){
    hangmanState = 0;
}
bool Player::addNewPlayer(const std::string &playerName)
{
    if (allPlayerNames.count(playerName) == 0) {
        allPlayerNames.insert(playerName);
        return true;
    }
    return false;
}
int Player::getRoomId(){
    return roomId;
}
bool Player::doesPlayerExist(const std::string &playerName)
{
    return allPlayerNames.count(playerName) > 0;
}
