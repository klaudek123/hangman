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
        }*/
          for (int i = 0; i < numEvents; ++i) {
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
                if(command == "register") {
                    std::string username = jsonData["username"].asString();

                    if(!Player::doesPlayerExist(username)){
                        Player newPlayer(username,events[i].data.fd);
                        game.addPlayertoMap(username,newPlayer);
                        Player::addNewPlayer(username); //po co nie wiem do sprawdzenia
                        Json::Value addresponse;
                        addresponse["command"] = "register";
                        addresponse["username"] = username;
                        addresponse["succes"] = true;
                        addresponse["message"] = "Nowy gracz dodany do gry";
                        std::string response = addresponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);

                    }else {
                        Json::Value errorresponse;
                        errorresponse["command"] = "register";
                        errorresponse["username"] = username;
                        errorresponse["succes"] = false;
                        errorresponse["message"] = "Nick zajety";
                        std::string response = errorresponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);

                    }



                }
                if(command == "enter_room"){
                    int roomId = jsonData["roomID"].asInt();
                    std::string username = jsonData["username"].asString();

                    bool playerExist = Player::doesPlayerExist(username);
                    if(!playerExist){
                        Json::Value errorResponse;
                        errorResponse["command"] = "enter_room";
                        errorResponse["username"] = username;
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
                            SuccesEnterResponse["command"] = "enter_room";
                            SuccesEnterResponse["username"] = username;
                            SuccesEnterResponse["succes"] = false;
                            SuccesEnterResponse["message"] = "Dodano gracza do pokoju";
                            std::string response = SuccesEnterResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                        }else{
                            Json::Value failEnterResponse;
                            failEnterResponse["command"] = "enter_room";
                            failEnterResponse["username"] = username;
                            failEnterResponse["succes"] = false;
                            failEnterResponse["message"] = "Nie udalo sie dolaczyc do pokoju";
                            std::string response = failEnterResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);

                        }


                    }

                }
                if(command == "leave_room"){
                    std::string username = jsonData["username"].asString();
                    bool playerExist = Player::doesPlayerExist(username);
                    if(!playerExist){
                        Json::Value errorResponse;
                        errorResponse["command"] = "leave_room";
                        errorResponse["username"] = username;
                        errorResponse["message"] = "Gracz nie istnieje";
                        errorResponse["succes"] = false;
                        std::string response = errorResponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);
                    }else {
                        Player* currentPlayer = game.getPlayerByUsername(username);
                        int roomId = currentPlayer->getRoomId();
                        if(game.removePlayerFromRoom(roomId,currentPlayer)){
                            currentPlayer->setRoomId(0);
                            Json::Value LeaveRoomResponse;
                            LeaveRoomResponse["command"] = "leave_room";
                            LeaveRoomResponse["usermane"] = username;
                            LeaveRoomResponse["succes"] = true;
                            LeaveRoomResponse["message"] = "Usunieto gracza z pokoju";
                            std::string response = LeaveRoomResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                        }
                        Json::Value failLeaveResponse;
                        failLeaveResponse["command"] = "leave_room";
                        failLeaveResponse["usermane"] = username;
                        failLeaveResponse["succes"] = false;
                        failLeaveResponse["message"] = "Nie udalo sie usunac gracza z pokoju";
                        std::string response = failLeaveResponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);

                    }

                }
                if(command == "start_game"){
                    std::string username = jsonData["username"].asString();
                    Player* currentPlayer = game.getPlayerByUsername(username);
                    int roomId = currentPlayer->getRoomId();
                    Room* room = game.getRoomById(roomId);
                    std::vector<Player> playersInRoom = room->getPlayers();
                    if(room->getNumPlayers()<2 or room->getFirstPlayer()!=username){
                        Json::Value FailStartResponse;
                        FailStartResponse["command"] = "start_game";
                        FailStartResponse["username"] = username;
                        FailStartResponse["succes"] = false;
                        FailStartResponse["message"] = "Nie mozna uruchomic";
                        std::string response = FailStartResponse.toStyledString();
                        send(events[i].data.fd, response.c_str(), response.length(), 0);

                    }else {
                        std::string word = game.getWordToGuess();
                        room->setWord(word);
                        for (const auto& player: playersInRoom) {
                            std::string gracz = player.getName();
                            Json::Value startResponse;
                            startResponse["command"] = "start_game";
                            startResponse["username"] = gracz;
                            startResponse["succes"] = true;
                            startResponse["message"] = "Gra rozpoczeta";
                            startResponse["word"] = word;
                            std::string response = startResponse.toStyledString();
                            send(player.getSocket(), response.c_str(), response.length(), 0);
                        }
                    }
                    }
                if(command == "guessed_letter"){
                    std::string username = jsonData["username"].asString();
                    std::string litera = jsonData["letter"].asString();
                    char letter = litera[0];
                    Player* currentPlayer = game.getPlayerByUsername(username);
                    int roomId = currentPlayer->getRoomId();
                    Room* room = game.getRoomById(roomId);
                    std::string word = room->getWord();
                    if(!room->isLetterGuessed(letter)){
                        if(word.find(letter)!= std::string::npos) {
                            currentPlayer->updateScore(1);
                        }else{
                            currentPlayer->updateHangmanState();
                        }
                        room->addGuessedLetter(letter);
                    }else{
                            /*juz zgadnieta*/
                        currentPlayer->updateHangmanState();
                        }
                    if(currentPlayer->getHangmanState()> 8){
                        currentPlayer->updateScore(-3);
                        currentPlayer->resetHangmanState();
                    }
                    bool slowo = true;
                    std::unordered_set<char> letters = room->getGuessed();
                    for(char ltr : word ){
                        if(ltr != ' ' && letters.find(ltr) == letters.end()){
                            slowo = false;
                            break;
                        }
                    }

                    std::vector<Player> playersInRoom = room->getPlayers();
                    Json::Value playersInfo(Json::arrayValue);
                    Json::Value guessedResponse;
                    guessedResponse["command"] = "guessed_letter";
                    if(slowo){
                        for(auto& player : playersInRoom){
                            player.resetHangmanState();
                        }
                        std::string new_word = game.getWordToGuess();
                        room->setWord(new_word);
                        guessedResponse["word"] = new_word;
                    }
                    for (const auto& gracze: playersInRoom){
                        Json::Value graczInfo;
                        graczInfo["username"] = gracze.getName();
                        graczInfo["score"] = gracze.getScore();
                        graczInfo["hangman_state"] = gracze.getHangmanState();
                        playersInfo.append(graczInfo);
                    }
                    for (const auto& player: playersInRoom) {

                        std::string gracz = player.getName();
                        guessedResponse["username"] = gracz;
                        guessedResponse["succes"] = true;
                        guessedResponse["players_info"] = playersInfo;

                        std::string response = guessedResponse.toStyledString();
                        send(player.getSocket(), response.c_str(), response.length(), 0);
                    }
                    }
                if(command == "get_players_in_room"){
                    std::string username = jsonData["username"].asString();
                    Player* currentPlayer = game.getPlayerByUsername(username);
                    int roomId = currentPlayer->getRoomId();
                    Room* room = game.getRoomById(roomId);
                    int players_amount = room->getNumPlayers();

                    Json::Value get_playersdResponse;
                    /*Json::Value playersInfo(Json::arrayValue);
                      for (const auto& player: playersInRoom) {
                        Json::Value graczInfo;
                        graczInfo["username"] = player.getName();
                        playersInfo.append(graczInfo);
                    }*/
                    get_playersdResponse["command"] = "get_players_in_room";
                    get_playersdResponse["username"] = username;
                    get_playersdResponse["players_amount"] = players_amount;
                    get_playersdResponse["room_number"] = roomId;
                    std::string response = get_playersdResponse.toStyledString();
                    send(events[i].data.fd, response.c_str(), response.length(), 0);
                }
                if(command == "get_available_rooms_count"){
                    std::string username = jsonData["get_available_rooms_count"].asString();
                    Json::Value get_roomsdResponse;
                    int number = game.getRoomsCount();
                    get_roomsdResponse["command"] = "get_players_in_room";
                    get_roomsdResponse["username"] = username;
                    get_roomsdResponse["rooms_amount"] = number;
                    std::string response = get_roomsdResponse.toStyledString();
                    send(events[i].data.fd, response.c_str(), response.length(), 0);

                }
                if(command == "new_room"){
                    std::string username = jsonData["username"].asString();
                    game.createNewRoom();
                    Json::Value newroomresponse;
                    newroomresponse["command"] = "new_room";
                    newroomresponse["username"] = username;
                    newroomresponse["succes"] = true;
                    newroomresponse["message"] = "Utworzono nowy pokoj";
                    std::string response = newroomresponse.toStyledString();
                    send(events[i].data.fd, response.c_str(), response.length(), 0);
                }
                if(command == "get_players_in_lobby"){
                    std::string username = jsonData["username"].asString();
                    Json::Value playersLobbyresponse;
                    playersLobbyresponse["command"] = "get_players_in_lobby";
                    playersLobbyresponse["username"] = username;
                    Json::Value jsonRooms;
                    for(const auto& pair : game.getGameRooms()){
                        int roomId = pair.first;
                        int numPlayers = pair.second.getNumPlayers();
                        Json::Value jsonRoom;
                        jsonRoom["room_id"] = roomId;
                        jsonRoom["num_players"] = numPlayers;
                        jsonRooms.append(jsonRoom);
                    }
                    playersLobbyresponse["room_players"] = jsonRooms;
                    std::string response = playersLobbyresponse.toStyledString();
                    send(events[i].data.fd, response.c_str(), response.length(), 0);
                }

            }
        }
    }

    close(sockfd);
    free(events); // Zwolnienie zaalokowanej pamięci dla tablicy zdarzeń
    return 0;
}
