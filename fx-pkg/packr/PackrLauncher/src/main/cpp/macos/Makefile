all: packr-mac-arm64

packr_macos.o: packr_macos.cpp
    g++ -o packr-macos-arm64 \
    -c packr_macos.cpp \
    -I .. \
    -I ../../headers \
    -I ../../../../../Jvm/include \
    -I ../../../../../Jvm/include/darwin \
    -I ../../../../../DrOpt/dropt/include \
    -std=c++11