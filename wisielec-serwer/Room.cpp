#include "Room.h"
#include "Player.h"
#include <algorithm>

Room::Room(){};
Room::~Room() {};

void Room::addPlayer(const Player& newPlayer) {
    players.push_back(newPlayer);
}
bool Room::getState() const {
    return state;
}
void Room::dropGuessed(){
    guessedLetters.clear();
};
void Room::changeState(bool gra) {
    state = gra;
}
int Room::getNumPlayers()const {
    return players.size();
}
std::string Room::getWord() {
    return wordtoguess;
}
bool Room::isLetterGuessed(char letter) const {
    return guessedLetters.find(letter) != guessedLetters.end();
}
std::string Room::getFirstPlayer() const {
    if(!players.empty()) {
        return players.front().getName();
    }else{
        return 0;
    }
}
void Room::addGuessedLetter(char letter) {
    guessedLetters.insert(letter);
}
std::unordered_set<char> Room::getGuessed(){
    return guessedLetters;
}
void Room::removePlayer(const Player& playertoRemove) {
    players.erase(std::remove_if(players.begin(), players.end(),
                                 [&](const Player& player)
    {return &player == &playertoRemove; }) ,
    players.end());
}
void Room::updateTime() {
    generationtime = std::chrono::steady_clock::now();
}
std::chrono::steady_clock::time_point Room::getTime() const {
    return generationtime;
}
void Room::setWord(std::string word) {
    wordtoguess = word;
}
const std::vector<Player>& Room::getPlayers() const {
   return players;
}
void Room::endGame() {
    // Logika zakończenia gry w pokoju
}
void Room::startGame() {

}
