@echo off

cd /d "%~dp0"

echo Compilando el docker...
call docker build -f ../docker/dockerfile.nginx -t nginx_img ../docker

echo Iniciando el contenedor...
call docker stop my-web
call docker rm my-web
call docker run -p 8080:80 --name my-web nginx_img