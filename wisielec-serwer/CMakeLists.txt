cmake_minimum_required(VERSION 3.12)
project(wisielec-serwer)

set(CMAKE_CXX_STANDARD 11)

# Dodaj pliki źródłowe
set(SOURCE_FILES
    src/main.cpp
    src/Game.cpp
    src/Player.cpp
    src/Room.cpp
)

# Dodaj pliki nagłówkowe do konfiguracji
include_directories(include)

# Dodaj bibliotekę jsoncpp
find_package(jsoncpp REQUIRED)

# Dodaj pliki źródłowe do konfiguracji
add_executable(main ${SOURCE_FILES})

# Podłącz bibliotekę jsoncpp
target_link_libraries(main jsoncpp)

# Dodaj plik slowo.txt do konfiguracji (kopiowanie do katalogu wyjściowego kompilacji)
configure_file(${CMAKE_SOURCE_DIR}/slowa.txt ${CMAKE_BINARY_DIR}/slowa.txt COPYONLY)
