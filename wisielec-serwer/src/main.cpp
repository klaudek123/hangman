#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <unistd.h>
#include <chrono>
#include <ctime>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <error.h>
#include <netdb.h>
#include <sys/epoll.h>
#include <unordered_set>
#include <list>
#include <jsoncpp/json/json.h>
#include <signal.h>
#include "Player.h"
#include "Game.h"
#include "Room.h"

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
    int sockfd = createSocket(8080); 
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
    //std::string haselko = game.getWordToGuess();
    while (true) {
        int numEvents = epoll_wait(epollfd, events, eventsCapacity, 0);
        if (numEvents == -1) {
            perror("Błąd epoll_wait()");
            exit(EXIT_FAILURE);
        }
        game.checkandremove();
        for (int i = 0; i < numEvents; ++i) {
            if (events[i].data.fd == sockfd) {
                // Obsługa nowego połączenia
                //int clientSocket = accept(sockfd, nullptr, nullptr);
                int clientSocket = accept4(sockfd, nullptr, nullptr, SOCK_NONBLOCK);
                if (clientSocket == -1) {
                    perror("Błąd accept4()");
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
                    //clientEvent.events = EPOLLIN;
                    clientEvent.events = EPOLLIN | EPOLLET;
                    clientEvent.data.fd = clientSocket;
                    if (epoll_ctl(epollfd, EPOLL_CTL_ADD, clientSocket, &clientEvent) == -1) {
                        perror("Błąd epoll_ctl() dla nowego klienta");
                        close(clientSocket);
                    }
                }
            }
            else {
                char buffer[1024];
                ssize_t bytesRead;
                
                while ((bytesRead = read(events[i].data.fd, buffer, sizeof(buffer))) > 0) {
                    // if(bytesRead == 0){
                    //     std::cout<<"odszedł sobie"<<std::flush;
                    // }
                    //else{
                        // Odczytano dane, możesz przetwarzać buffer
                        std::cout<<"bytesRead: "<<bytesRead<<std::flush<<std::endl;
                        // Przetworzenie otrzymanych danych jako JSON
                        Json::Value jsonData; // Używamy typu Json::Value z biblioteki jsoncpp
                        Json::CharReaderBuilder builder;
                        Json::CharReader *reader = builder.newCharReader();
                        std::string errors;
                        bool parsingSuccessful = reader->parse(buffer, buffer + bytesRead, &jsonData, &errors);
                        delete reader;

                        if (!parsingSuccessful) {
                            std::cerr << "Błąd parsowania JSON: " << errors << std::endl;
                            continue;
                        }
                        std::cout<<"request: "<<jsonData<<std::flush<<std::endl;
                        std::string command = jsonData["command"].asString();
                        if (command == "register") {
                            std::string username = jsonData["username"].asString();

                            // Tu uzupełnij odpowiedź w formie JSON
                            Json::Value response;

                            if (!Player::doesPlayerExist(username)) {
                                Player newPlayer(username, events[i].data.fd);
                                game.addPlayertoMap(username, newPlayer);
                                Player::addNewPlayer(username);
                                response["command"] = "register";
                                response["username"] = username;
                                response["success"] = true;
                                //response["message"] = "Nowy gracz dodany do gry";
                            } else {
                                response["command"] = "register";
                                response["username"] = username;
                                response["success"] = false;
                                //response["message"] = "Nick zajety";
                            }

                            Json::StreamWriterBuilder wbuilder;
                            std::string jsonResponse = Json::writeString(wbuilder, response) + '\n';

                            send(events[i].data.fd, jsonResponse.c_str(), jsonResponse.length(), 0);
                        }

                        if(command == "get_available_rooms_count"){
                            std::string username = jsonData["username"].asString();
                            Json::Value get_roomsdResponse;
                            int number = game.getRoomsCount();
                            get_roomsdResponse["command"] = "get_available_rooms_count";
                            get_roomsdResponse["username"] = username;
                            get_roomsdResponse["rooms_amount"] = number;

                            Json::StreamWriterBuilder wbuilder;
                            std::string jsonResponse = Json::writeString(wbuilder, get_roomsdResponse) + '\n';

                            send(events[i].data.fd, jsonResponse.c_str(), jsonResponse.length(), 0);

                        }
                        if(command == "get_players_in_lobby"){
                            int roomid =jsonData["room_number"].asInt();
                            std::string username = jsonData["username"].asString();
                            Player* currentPlayer = game.getPlayerByUsername(username);
                            currentPlayer->updateTime();
                            Room* room = game.getRoomById(roomid);
                            int amount = room->getNumPlayers();
                            std::cout<<amount<<std::flush;
                            Json::Value playersLobbyresponse;
                            playersLobbyresponse["command"] = "get_players_in_lobby";
                            playersLobbyresponse["username"] = username;
                            playersLobbyresponse["room_number"] = roomid;
                            playersLobbyresponse["num_players"] = amount;
                            std::string response = playersLobbyresponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                        }

                        if(command == "get_players_in_room"){
                            std::string username = jsonData["username"].asString();
                            std::cout<<username<<std::flush;
                            Player* currentPlayer = game.getPlayerByUsername(username);
                            int roomId = currentPlayer->getRoomId();
                            std::cout<<roomId<<std::flush;
                            Room* room = game.getRoomById(roomId);
                            int players_amount = room->getNumPlayers();
                            std::cout<<players_amount<<std::flush;
                            Json::Value get_playersResponse;
                            /*Json::Value playersInfo(Json::arrayValue);
                            for (const auto& player: playersInRoom) {
                                Json::Value graczInfo;
                                graczInfo["username"] = player.getName();
                                playersInfo.append(graczInfo);
                            }*/
                            get_playersResponse["command"] = "get_players_in_room";
                            get_playersResponse["username"] = username;
                            get_playersResponse["players_amount"] = players_amount;
                            get_playersResponse["room_number"] = roomId;
                            Json::StreamWriterBuilder wbuilder;
                            std::string response = Json::writeString(wbuilder, get_playersResponse) + '\n';

                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                        }

                        if(command == "enter_room"){
                            int roomId = jsonData["room_number"].asInt();
                            std::string username = jsonData["username"].asString();

                            //bool playerExist = Player::doesPlayerExist(username);
                            if(false){
                                Json::Value errorResponse;
                                errorResponse["command"] = "enter_room";
                                errorResponse["username"] = username;
                                errorResponse["success"] = false;
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
                                    SuccesEnterResponse["room_number"] = roomId;
                                    SuccesEnterResponse["success"] = true;
                                    //SuccesEnterResponse["message"] = "Dodano gracza do pokoju";
                                    Json::StreamWriterBuilder wbuilder;
                                    std::string response = Json::writeString(wbuilder, SuccesEnterResponse) + '\n';
                                
                                    send(events[i].data.fd, response.c_str(), response.length(), 0);
                                }else{
                                    Json::Value failEnterResponse;
                                    failEnterResponse["command"] = "enter_room";
                                    failEnterResponse["username"] = username;
                                    failEnterResponse["room_number"] = roomId;
                                    failEnterResponse["success"] = false;
                                    //failEnterResponse["message"] = "Nie udalo sie dolaczyc do pokoju";
                                    Json::StreamWriterBuilder wbuilder;
                                    std::string response = Json::writeString(wbuilder, failEnterResponse) + '\n';
                                    
                                    send(events[i].data.fd, response.c_str(), response.length(), 0);
                                }
                            }
                        }
                        if(command == "leave_room"){
                        std::string username = jsonData["username"].asString();
                        bool playerExist = game.doesPlayerexist(username);
                        if(!playerExist){
                            Json::Value errorResponse;
                            errorResponse["command"] = "leave_room";
                            errorResponse["username"] = username;
                            errorResponse["message"] = "Gracz nie istnieje";
                            errorResponse["success"] = false;
                            std::string response = errorResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                        }else {
                                Player* currentPlayer = game.getPlayerByUsername(username);
                                int roomId = currentPlayer->getRoomId();
                                Room* room = game.getRoomById(roomId);
                                if(game.removePlayerFromRoom(roomId,currentPlayer)) {
                                    currentPlayer->setRoomId(0);
                                    currentPlayer->resetScore();
                                    const Player& playerref = *currentPlayer;
                                    room->removePlayer(playerref);
                                    currentPlayer->resetHangmanState();
                                    Json::Value LeaveRoomResponse;
                                    LeaveRoomResponse["command"] = "leave_room";
                                    LeaveRoomResponse["username"] = username;
                                    LeaveRoomResponse["success"] = true;
                                    LeaveRoomResponse["message"] = "Usunieto gracza z pokoju";
                                    std::string response = LeaveRoomResponse.toStyledString();
                                    send(events[i].data.fd, response.c_str(), response.length(), 0);
                                    Room *room = game.getRoomById(roomId);
                                    if (room->getNumPlayers() < 2) {

                                        if (room->getState()) {
                                            room->setWord("");
                                            std::vector<Player> playersInRoom = room->getPlayers();
                                            for (auto player: playersInRoom) {
                                                player.resetHangmanState();
                                                player.resetScore();
                                            }
                                            room->changeState(false);
                                        }

                                    }
                                }
                                Json::Value failLeaveResponse;
                                failLeaveResponse["command"] = "leave_room";
                                failLeaveResponse["username"] = username;
                                failLeaveResponse["success"] = false;
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
                            FailStartResponse["success"] = false;
                            //FailStartResponse["message"] = "Nie mozna uruchomic";
                            std::string response = FailStartResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);

                        }else {
                            std::string word = game.getWordToGuess();
                            room->setWord(word);
                            room->updateTime();
                            room->changeState(true);
                            Json::Value startResponse;
                            startResponse["command"] = "start_game";
                            startResponse["username"] = username;
                            startResponse["success"] = true;
                            //startResponse["message"] = "Gra rozpoczeta";
                            std::string response = startResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                        }
                        }
                        if(command == "guessed_letter") {
                                std::string username = jsonData["username"].asString();
                                std::string litera = jsonData["letter"].asString();
                                char letter = litera[0];
                                std::cout<<letter<<std::flush;
                                std::cout<<"1"<<std::flush;
                                Player* currentPlayer = game.getPlayerByUsername(username);
                                std::cout<<"11"<<std::flush;
                                int roomId = currentPlayer->getRoomId();
                                Room* room = game.getRoomById(roomId);
                                //std::string word = room->getWord();
                                std::string word = room->getWord();

                                if(!room->isLetterGuessed(letter)) {
                                    if(word.find(letter) != std::string::npos) {
                                        // Poprawna zgadywana litera - aktualizacja punktów gracza
                                        currentPlayer->updateScore(1);
                                        int score2 = currentPlayer->getScore();
                                        std::cout<<"updateScore!"<<score2<<"pkt"<<currentPlayer->getName()<<"<-imie"<<std::flush;

                                    } else {
                                        // Błędna zgadywana litera - aktualizacja stanu wisielca gracza
                                        currentPlayer->updateHangmanState();
                                        std::cout<<"updateHangman!"<<std::flush;
                                    }
                                    room->addGuessedLetter(letter);
                                } else {
                                    // Litera już zgadnięta - aktualizacja stanu wisielca gracza
                                    currentPlayer->updateHangmanState();
                                }
                                std::cout<<"2"<<std::flush;

                                if(currentPlayer->getHangmanState() > 8) {
                                    // Gracz przekroczył limit błędnych zgadywań - aktualizacja punktów i reset stanu wisielca
                                    currentPlayer->updateScore(-3);
                                    currentPlayer->resetHangmanState();
                                }

                                bool slowo = true;
                                std::unordered_set<char> letters = room->getGuessed();
                                for(char ltr : word) {
                                    if(ltr != ' ' && letters.find(ltr) == letters.end()) {
                                        // Sprawdzenie, czy całe słowo zostało odgadnięte
                                        slowo = false;
                                        break;
                                    }
                                }
                                std::cout<<"3"<<std::flush;
                                std::vector<Player> playersInRoom = room->getPlayers();
                                Json::Value playersInfo(Json::arrayValue);
                                std::cout<<slowo<<std::flush;
                                if(slowo){
                                    for(auto& player : playersInRoom){
                                        std::string nazwa = player.getName();
                                        Player* gracz = game.getPlayerByUsername(nazwa);
                                        gracz->resetHangmanState();
                                    }
                                    room->dropGuessed();
                                    std::string new_word = game.getWordToGuess();
                                    room->setWord(new_word);
                                    room->updateTime();
                                }
                                std::cout<<"4"<<std::flush;
                                // Tworzenie informacji o graczach w pokoju
                                // for(const auto& player : playersInRoom) {
                                //     Json::Value graczInfo;
                                //     graczInfo["username"] = player.getName();
                                //     graczInfo["score"] = player.getScore();
                                //     graczInfo["hangman_state"] = player.getHangmanState();
                                //     playersInfo.append(graczInfo);
                                // }
                                std::cout<<"5"<<std::flush;
                                // Wysłanie informacji do wszystkich graczy w pokoju
                                //for(const auto& player : playersInRoom) {
                                    Json::Value guessedResponse;
                                    guessedResponse["command"] = "guessed_letter";
                                    //std::string gracz = player.getName();
                                    guessedResponse["username"] = username;
                                    guessedResponse["success"] = true;
                                    guessedResponse["players_info"] = playersInfo;
                                    guessedResponse["word"] = room->getWord();

                                    // Konwersja JSON na string i wysłanie go na gniazdo gracza
                                    std::string response = guessedResponse.toStyledString();
                                    std::cout<<response<<std::flush;
                                    send(events[i].data.fd, response.c_str(), response.length(), 0);
                                //}
                            }
                            
                        if(command  == "get_room_player_info"){
                            std::string username = jsonData["username"].asString();
                            Player* currentPlayer = game.getPlayerByUsername(username);
                            currentPlayer->updateTime();
                            int roomId = currentPlayer->getRoomId();
                            Room* room = game.getRoomById(roomId);
                            std::vector<Player> playersInRoom = room->getPlayers();
                            Json::Value playersInfores;
                            Json::Value playersInfo(Json::arrayValue);
                            std::cout<<"currPlayScore: "<<currentPlayer->getScore()<<std::flush;
                            for (const auto& player: playersInRoom) {
                                Json::Value graczInfo;
                                std::string user =   player.getName();
                                graczInfo["username"] = user;
                                Player* gracz = game.getPlayerByUsername(user);
                                graczInfo["score"] = gracz->getScore();
                                graczInfo["hangman_state"] = gracz->getHangmanState();
                                playersInfo.append(graczInfo);
                            }
                            playersInfores["command"] = "get_players_in_room";
                            playersInfores["username"] = username;
                            playersInfores["word"] = room->getWord();
                            playersInfores["time"] = room->getTime();
                            std::unordered_set<char> gues = room->getGuessed();
                            std::string res;
                            for(const auto& character : gues){
                                res += character;
                            }
                            playersInfores["guessed_letters"] = res;
                            playersInfores["info"] = playersInfo;
                            std::string response = playersInfores.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);

                        }
                        if(command == "time_end"){
                            std::string username = jsonData["username"].asString();
                            Player* currentPlayer = game.getPlayerByUsername(username);
                            int roomId = currentPlayer->getRoomId();
                            Room* room = game.getRoomById(roomId);
                            if(room->getFirstPlayer()==username){
                                std::string word = game.getWordToGuess();
                                room->setWord(word);
                                room->updateTime();
                                Json::Value startResponse;
                                startResponse["command"] = "time_end";
                                startResponse["username"] = username;
                                std::string response = startResponse.toStyledString();
                                send(events[i].data.fd, response.c_str(), response.length(), 0);
                            }else{
                            Json::Value startResponse;
                            startResponse["command"] = "time_end";
                            startResponse["username"] = username;
                            std::string response = startResponse.toStyledString();
                            send(events[i].data.fd, response.c_str(), response.length(), 0);
                            }

                        }
                        // Czyść bufor
                        memset(buffer, 0, sizeof(buffer));
                    //}
                }                
            }
        }
        //game.checkandremove();
    }


    close(sockfd);
    free(events); // Zwolnienie zaalokowanej pamięci dla tablicy zdarzeń
    return 0;
}

