@echo off

set BUILD_DIR=dist
set END_DIR=../docker/compiledPage

cd /d "%~dp0"

echo Instalando dependencias...
call npm install

echo Compilando pagina...
call npm run build

echo Borrando ficheros antiguos...
if exist "%END_DIR%" (
    rmdir /s /q %END_DIR%
)
mkdir "%END_DIR%"

echo Copiando a la ruta final...
xcopy "%BUILD_DIR%" "%END_DIR%" /E /I /Y
