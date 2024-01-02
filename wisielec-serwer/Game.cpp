#include "Game.h"

Game::Game()
{

}

Game::~Game()
{

}

void Game::addPlayerToRoom(int roomId, const Player& newPlayer) {
    gameRooms[roomId].addPlayer(newPlayer); // Dodanie gracza do konkretnego pokoju
}

std::string Game::getWordToGuess() {
    return wordToGuess;
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