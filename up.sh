#!/bin/bash

# Aborta o script em caso de erro
set -e

echo "🚀 Sincronizando dependências JS..."
./gradlew kotlinUpgradeYarnLock --no-daemon

echo "🚀 Iniciando build local do projeto (Server e JS-Dev)..."
./gradlew :server:installDist :composeApp:jsBrowserDevelopmentExecutableDistribution -Pandroid.useAndroidX=true --no-daemon

echo "✅ Build concluído! Preparando arquivos para o Docker..."

# Limpamos e criamos a estrutura de deploy
rm -rf deploy
mkdir -p deploy/server deploy/web

# Localizamos a pasta de instalação do servidor (o nome pode variar)
SERVER_INSTALL_DIR=$(find server/build/install -maxdepth 1 -mindepth 1 -type d | head -n 1)
echo "📦 Copiando servidor de: $SERVER_INSTALL_DIR"
cp -R "$SERVER_INSTALL_DIR"/* deploy/server/

# Copiamos o bundle da web
echo "📦 Copiando web de: composeApp/build/dist/js/developmentExecutable"
cp -R composeApp/build/dist/js/developmentExecutable/* deploy/web/

echo "🐳 Subindo os containers Docker..."
# Forçamos o rebuild para garantir que o nginx.conf e os novos binários sejam aplicados
docker-compose up --build -d

echo "--------------------------------------------------------"
echo "✨ Sistema online!"
echo "🌐 Web (JS): http://localhost:8081"
echo "🖥️  Server: http://localhost:8080"
echo "--------------------------------------------------------"
echo "DICA: Se o erro de conexão persistir, rode: docker-compose logs -f server"
