cmake_minimum_required(VERSION 3.10)
project(virtualcam)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(virtualcam SHARED
    src/main.cpp
    src/VirtualCamBridge.cpp)

target_include_directories(virtualcam PRIVATE include)

target_link_libraries(virtualcam log)
