### runj，一个跨平台的java启动器  
#### windows amd64编译
gcc -o runj_win_amd64 .\runj_win.c -mwindows -luser32 -lgdi32 
#### linux amd64编译
gcc -o runj_linux_amd64 .\runj_linux.c 
#### linux arm64编译
gcc -o runj_linux_arm64 .\runj_linux.c
#### macos amd64编译
gcc -o runj_mac_amd64 .\runj_mac.c  
#### macos amd64编译
g++ -o runj_mac_amd64 .\runj_mac.cpp   
