#!/bin/bash

BUILD_DIR=dist
END_DIR=../docker/compiledPage

echo Instalando dependencias...
npm install

echo Compilando pagina...
npm run build

echo Borrando ficheros antiguos...

if [ -d "$END_DIR" ]; then
    rm -rf $END_DIR
fi

mkdir "$END_DIR"

echo Copiando a la ruta final...

cp -r "./$BUILD_DIR/." "./$END_DIR/"
