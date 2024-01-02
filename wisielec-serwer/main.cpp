#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <error.h>
#include <netdb.h>
#include <sys/epoll.h>
#include <unordered_set>
#include <list>
#include <signal.h>


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

    while (true) {
        int numEvents = epoll_wait(epollfd, events, eventsCapacity, -1);
        if (numEvents == -1) {
            perror("Błąd epoll_wait()");
            exit(EXIT_FAILURE);
        }

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
                if (command == "check_existence") {
                    std::string username = jsonData["username"].asString();
                    // Tutaj dodaj kod, który sprawdzi istnienie gracza o nazwie "username"
                    // i wyśle odpowiedź w formie JSON

                    Json::Value jsonResponse;
                    // Tu uzupełnij odpowiedź w formie JSON
                    bool exists = Player::doesPlayerExist(username); // Przykład odpowiedzi - zawsze zwrac
                    jsonResponse["exists"] = exists;

                    std::string response = jsonResponse.toStyledString();
                    send(events[i].data.fd, response.c_str(), response.length(), 0);
                }
            }
        }
    }

    close(sockfd);
    free(events); // Zwolnienie zaalokowanej pamięci dla tablicy zdarzeń
    return 0;
}