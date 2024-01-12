#include "Game.h"
#include <fstream>
#include <iostream>
#include <cstdlib>
#include <ctime>


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
Room* Game::getRoomById(int roomId) {
    auto it = gameRooms.find(roomId);
    if(it != gameRooms.end()){
        return &(it->second);
    }else{
        return 0;
    }
}
bool Game::addPlayerToRoom(int roomId, Player* player) {
    if(player)
    {
        gameRooms[roomId].addPlayer(*player);
        return true;
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
void Game::createNewRoom(){
    int newRoomId = gameRooms.size() + 1;
    Room newRoom;
    gameRooms[newRoomId] = newRoom;
}
std::string Game::getWordToGuess() {
    std::ifstream file("slowa.txt");
    if(!file.is_open()){
        return 0;
    }
    int w = 115;
    srand(static_cast<unsigned int>(time(nullptr)));
    int rnd = rand() % w;
    int curr = 0;
    std::string word;
    std::string line;
    while(std::getline(file, line)){
        if(curr == rnd){
            word = line;
        }
        curr++;
    }
    file.close();
    return word;
}
int Game::getRoomsCount() const {
    return gameRooms.size();
}
bool Game::doesPlayerexist(const std::string username) const {
    return playersMap.find(username) != playersMap.end();
}
void Game::checkandremove(){
    auto curtime = std::chrono::steady_clock::now();
    for(auto it = playersMap.begin(); it != playersMap.end();)
    {
        auto& player = it -> second;
        auto lastRqTime = player.getTime();

        auto time = std::chrono::duration_cast<std::chrono::seconds>(curtime-lastRqTime).count();
        if(time > 10){
            Player* gracz = &player;
            int id = player.getRoomId();
            removePlayerFromRoom(id,gracz);
            it = playersMap.erase(it);
        }else{
            it++;
        }
    }
}
void Game::updateGameState() {
    // Logika aktualizacji stanu gry po każdym ruchu gracza
}
