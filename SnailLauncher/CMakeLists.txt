# 设置最低版本
cmake_minimum_required(VERSION 3.0)
# 设置项目名称
project(SnailLauncher)
# 设置字符集
add_definitions(-DUNICODE -D_UNICODE)
# 设置共享DLL
#add_definitions(-D_AFXDLL)
#set(CMAKE_MFC_FLAG 2)
# 设置静态DLL
set(CMAKE_MFC_FLAG 1)
set(CMAKE_CXX_FLAGS_RELEASE "/MT")
set(CMAKE_CXX_FLAGS_DEBUG "/MTd")
# 设置源码路径
add_subdirectory(src)
