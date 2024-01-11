#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <error.h>
#include <nlohmann/json.hpp>
#include <netdb.h>
#include <sys/epoll.h>
#include <unordered_set>
#include <list>
#include <jsoncpp/json/json.h>
#include <signal.h>
#include "Player.h"
#include "Game.h"


// Funkcja do tworzenia gniazda
int createSocket(int port) {
    int sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd == -1) {
        std::cerr << "Błąd przy tworzeniu gniazda" << std::endl;
        exit(EXIT_FAILURE);
    }

    int opt = 1;
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt))) {
        std::cerr << "Błąd przy ustawianiu opcji gniazda" << std::endl;
        exit(EXIT_FAILURE);
    }

    struct sockaddr_in serv_addr;
    memset(&serv_addr, '0', sizeof(serv_addr));

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(port);

    if (bind(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1) {
        std::cerr << "Błąd przy wiązaniu gniazda" << std::endl;
        exit(EXIT_FAILURE);
    }

    if (listen(sockfd, SOMAXCONN) == -1) {
        std::cerr << "Błąd przy nasłuchiwaniu na gnieździe" << std::endl;
        exit(EXIT_FAILURE);
    }

    return sockfd;
}

int main() {
    int sockfd = createSocket(12345); // Przykładowy port 12345

    int epollfd = epoll_create1(0);
    if (epollfd == -1) {
        perror("Błąd epoll_create1()");
        exit(EXIT_FAILURE);
    }

    struct epoll_event event;
    event.events = EPOLLIN;
    event.data.fd = sockfd;

    if (epoll_ctl(epollfd, EPOLL_CTL_ADD, sockfd, &event) == -1) {
        perror("Błąd epoll_ctl()");
        exit(EXIT_FAILURE);
    }

    const int INITIAL_EVENTS = 16; // Początkowy rozmiar tablicy zdarzeń
    struct epoll_event* events = (struct epoll_event*)malloc(sizeof(struct epoll_event) * INITIAL_EVENTS);
    if (events == nullptr) {
        perror("Błąd alokacji pamięci dla tablicy zdarzeń");
        exit(EXIT_FAILURE);
    }

    int eventsCapacity = INITIAL_EVENTS;
    Game game;
    while (true) {
        int numEvents = epoll_wait(epollfd, events, eventsCapacity, -1);
        if (numEvents == -1) {
            perror("Błąd epoll_wait()");
            exit(EXIT_FAILURE);
        }/* logika sprawdzajca czy pokoje sa pelne aby dodac nowy pokoj,
 * imo dalbym konstruktor nowego pokoju w Game zeby dzialalo na przycsik ze dodaje nowy pokoj jak tylko bedziesz chcial
        bool allRoomsFull = true;
        std::unordered_map<int, Room>& rooms = game.getGameRooms();
        for(const auto& pair : rooms){
            if (pair.second.getNumPlayers()){
                allRoomsFull = false;
                break;
            }
        }
        *//*if(allRoomsFull){
            int newRoomId = rooms.size() + 1;
            Room newRoom(newRoomId);
            rooms[newRoomId] = newRoom;
        }*//*
*/        for (int i = 0; i < numEvents; ++i) {
            if (events[i].data.fd == sockfd) {
                // Obsługa nowego połączenia
                int clientSocket = accept(sockfd, nullptr, nullptr);
                if (clientSocket == -1) {
                    perror("Błąd accept()");
                } else {
                    // Rozszerzenie tablicy zdarzeń, jeśli przekroczono jej pojemność
                    if (numEvents >= eventsCapacity) {
                        eventsCapacity *= 2; // Podwajanie pojemności tablicy
                        events = (struct epoll_event*)realloc(events, sizeof(struct epoll_event) * eventsCapacity);
                        if (events == nullptr) {
                            perror("Błąd realokacji pamięci dla tablicy zdarzeń");
                            exit(EXIT_FAILURE);
                        }
                    }

                    // Dodanie nowego klienta do monitorowanych zdarzeń
                    struct epoll_event clientEvent;
                    clientEvent.events = EPOLLIN;
                    clientEvent.data.fd = clientSocket;
                    if (epoll_ctl(epollfd, EPOLL_CTL_ADD, clientSocket, &clientEvent) == -1) {
                        perror("Błąd epoll_ctl() dla nowego klienta");
                        close(clientSocket);
                    }
                }
            } else {
                // Obsługa istniejącego połączenia
                // Odbieranie danych od klienta, wysyłanie odpowiedzi, itp.
                char buffer[1024] = {0};
                int valread = read(events[i].data.fd, buffer, 1024);
                if (valread <= 0) {
                    close(events[i].data.fd);
                    continue;
                }

                // Przetworzenie otrzymanych danych jako JSON
                std::string receivedData = std::string(buffer);
                Json::Value jsonData;
                Json::Reader jsonReader;
                bool parsingSuccessful = jsonReader.parse(receivedData, jsonData);
                if (!parsingSuccessful) {
                    std::cerr << "Błąd parsowania JSON" << std::endl;
                    continue;
                }

                std::string command = jsonData["command"].asString();
                if (command == "Register") {
                    std::string username = jsonData["username"].asString();

                    if(!Player::doesPlayerExist(username)){
                        Player newPlayer(username,events[i].data.fd);
                        game.addPlayertoMap(username,newPlayer);
                        Player::addNewPlayer(username); //po co nie wiem do sprawdzenia
                        Json::Value addresponse;
                        addresponse["succes"] = true;
                        addresponse["message"] = "Nowy gracz dodany do gry";
                        std::string response = addresponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);

                    }else {
                        Json::Value errorresponse;
                        errorresponse["succes"] = false;
                        errorresponse["message"] = "Nick zajety";
                        std::string response = errorresponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);

                    }



                }
                if(command == "Enter_room"){
                    int roomId = jsonData["roomID"].asInt();
                    std::string username = jsonData["username"].asString();

                    bool playerExist = Player::doesPlayerExist(username);
                    if(!playerExist){
                        Json::Value errorResponse;
                        errorResponse["succes"] = false;
                        errorResponse["message"] = "Gracz nie istnieje";
                        std::string response = errorResponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);
                    }else {
                        Player* currentPlayer = game.getPlayerByUsername(username);
                        //currentPlayer->setRoomId(roomId);
                        if(game.addPlayerToRoom(roomId,currentPlayer)){
                            currentPlayer->setRoomId(roomId);
                            Json::Value SuccesEnterResponse;
                            SuccesEnterResponse["succes"] = true;
                            SuccesEnterResponse["message"] = "Dolaczono do pokoju";
                            std::string response = SuccesEnterResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                        }else{
                            Json::Value failEnterResponse;
                            failEnterResponse["succes"] = false;
                            failEnterResponse["message"] = "Nie udalo sie dolaczyc do pokoju";
                            std::string response = failEnterResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);

                        }


                    }

                }
                if(command == "Leave_Room"){
                    std::string username = jsonData["username"].asString();
                    bool playerExist = Player::doesPlayerExist(username);
                    if(!playerExist){
                        Json::Value errorResponse;
                        errorResponse["error"] = "Gracz nie istnieje";
                        errorResponse["succes"] = false;
                        std::string response = errorResponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);
                    }else {
                        Player* currentPlayer = game.getPlayerByUsername(username);
                        int roomId = currentPlayer->getRoomId();
                        if(game.removePlayerFromRoom(roomId,currentPlayer)){
                            currentPlayer->setRoomId(0);
                            Json::Value LeaveRoomResponse;
                            LeaveRoomResponse["succes"] = true;
                            std::string response = LeaveRoomResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                        }
                        Json::Value failLeaveResponse;
                        failLeaveResponse["error"] = "Nie udalo sie opuscic do pokoju";
                        failLeaveResponse["succes"] = false;
                        std::string response = failLeaveResponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);

                    }

                }
                if(command == "Start_Game"){
                    std::string username = jsonData["username"].asString();
                    Player* currentPlayer = game.getPlayerByUsername(username);
                    int roomId = currentPlayer->getRoomId();
                    Room* room = game.getRoomById(roomId);
                    std::vector<Player> playersInRoom = room->getPlayers();
                    if(room->getNumPlayers()<2 or room->getFirstPlayer()!=username){
                        Json::Value FailStartResponse;
                        FailStartResponse["Succes"] = false;
                        FailStartResponse["message"] = "Nie mona uruchomi c ";
                        std::string response = FailStartResponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);

                    }else {
                        std::string word = game.getWordToGuess();
                        room->setWord(word);
                        for (const auto& player: playersInRoom) {

                            Json::Value startResponse;
                            startResponse["Game"] = true;
                            startResponse["Word"] = word;
                            std::string response = startResponse.toStyledString();
                            send(player.getSocket(), response.c_str(), response.length(), 0);
                        }
                    }
                    }
                if(command =="Guessed_letter"){
                    std::string username = jsonData["username"].asString();
                    std::string ltr = jsonData["letter"].asString();
                    char letter = ltr[0];


                    Player* currentPlayer = game.getPlayerByUsername(username);
                    int roomId = currentPlayer->getRoomId();
                    Room* room = game.getRoomById(roomId);
                    std::string word = room->getWord();
                    if(!room->isLetterGuessed(letter)){
                        if(word.find(letter)!= std::string::npos) {
                            currentPlayer->updateScore();
                            /*wiadomocs o sukcesie*/


                        }else{
                            currentPlayer->updateHangmanState();
                        }
                        room->addGuessedLetter(letter);
                    }else{
                            /*juz zgadnieta*/
                        currentPlayer->updateHangmanState();
                        }
                    bool slowo = true;

                    std::unordered_set<char> letters = room->getGuessed();
                    for(char ltr : word ){
                        if(ltr != ' ' && letters.find(ltr) == letters.end()){
                            slowo = false;
                            break;
                        }
                    }
                    if(slowo){
                        std::vector<Player> playersInRoom = room->getPlayers();
                        std::string word = game.getWordToGuess();
                        room->setWord(word);
                        for(auto& player : playersInRoom){
                            player.resetHangmanState();
                            Json::Value startresponse;
                            startresponse["New_word"] = true;
                            startresponse["Word"] = word;
                            std::string response = startresponse.toStyledString();
                            send(player.getSocket(), response.c_str(), response.length(), 0);
                        }
                    if(currentPlayer->getHangmanState()> 8){
                        
                    }
                    }
                    }


            }
        }
    }

    close(sockfd);
    free(events); // Zwolnienie zaalokowanej pamięci dla tablicy zdarzeń
    return 0;
}
