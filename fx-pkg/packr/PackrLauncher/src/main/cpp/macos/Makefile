all: packr-mac

packr-mac: packr_macos.o
    gcc -o packr-mac packr_macos.o

packr_macos.o: packr_macos.cpp
    gcc -c packr_macos.cpp \
    -I .. \
    -I ../../headers \
    -I ../../../../../Jvm/include \
    -I ../../../../../Jvm/include/darwin \
    -I ../../../../../DrOpt/dropt/include \
    -std=c++17 \
    -g \
    -fcolor-diagnostics \
    -fansi-escape-codes \
    CGO_CFLAGS="-I$(brew --prefix)/opt/gdal/include" CGO_LDFLAGS="-L$(brew --prefix)/opt/gdal/lib -lgdal"
