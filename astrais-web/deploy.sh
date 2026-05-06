#!/bin/bash

echo Compilando el docker...
docker build -f ../docker/dockerfile.nginx -t nginx_img ../docker

echo Iniciando el contenedor...
docker stop my-web
docker rm my-web
docker run -p 8080:80 --name my-web nginx_img
