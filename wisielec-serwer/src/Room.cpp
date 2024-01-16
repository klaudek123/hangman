#include "Room.h"
#include "Player.h"
#include <algorithm>

Room::Room(){};
Room::~Room() {};

std::vector<Player> players;

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
                                { return player.getName() == playertoRemove.getName(); }),
    players.end());
}
void Room::updateTime() {
    generationtime = std::chrono::system_clock::now();
}

std::string Room::getTime() const {
        auto timePoint = std::chrono::system_clock::to_time_t(generationtime);
        std::tm* timeinfo = std::localtime(&timePoint);

        std::ostringstream oss;
        oss << std::setfill('0') << std::setw(2) << timeinfo->tm_hour << ":"
            << std::setfill('0') << std::setw(2) << timeinfo->tm_min << ":"
            << std::setfill('0') << std::setw(2) << timeinfo->tm_sec;

        return oss.str();
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

