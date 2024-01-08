#include "Game.h"

Game::Game()
{
    for(int i =0; i < 4; ++i){
        Room newRoom;
        gameRooms[i + 1] = newRoom;
    }

}

Game::~Game()
{

}
Player* Game::getPlayerByUsername(const std::string& username){
    auto it = playersMap.find(username);
    if(it != playersMap.end()){
        return &(it->second);
    }
    return nullptr;
}
bool Game::addPlayerToRoom(int roomId, Player* player) {
    if(player)
    {
        if(gameRooms[roomId].getNumPlayers()<10){
            gameRooms[roomId].addPlayer(*player);
            return true;
        }
    }
    return false;
}
bool Game::removePlayerFromRoom(int roomId, Player* player){
    if(player){
        gameRooms[roomId].removePlayer(*player);
        return true;
    }
    return false;
}
std::unordered_map<int,Room>&Game::getGameRooms(){
    return gameRooms;
}
void Game::addPlayertoMap(const std::string& username, const Player& player){
    playersMap.insert({username, player});
}
std::string Game::getWordToGuess() {
    return wordToGuess;
}
void Game::createNewRoom(const Player& player){
    int newRoomId = gameRooms.size() + 1;
    Room newRoom;
    gameRooms[newRoomId] = newRoom;
    newRoom.addPlayer(player);


}

void Game::setWordToGuess(std::string word) {
    wordToGuess = word;
}

void Game::guessLetter(Player player, char letter) {
    // Logika zgadnięcia litery przez gracza
}

void Game::updateGameState() {
    // Logika aktualizacji stanu gry po każdym ruchu gracza
}
