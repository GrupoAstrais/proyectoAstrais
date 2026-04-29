#!/bin/bash

echo Compilando el docker...
sudo docker build -f ../docker/dockerfile.nginx -t nginx_img ../docker

echo Iniciando el contenedor...
sudo docker run -d -p 8080:80 --name my-web nginx_img