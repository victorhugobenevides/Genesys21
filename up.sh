#!/bin/bash

# Aborta o script em caso de erro
set -e

# Aumenta a memória disponível para o processo do Node/Webpack
export NODE_OPTIONS="--max-old-space-size=4096"

echo "🛑 Parando containers..."
docker-compose down -v --remove-orphans

echo "🧹 Limpando caches do Kotlin/JS..."
./gradlew clean --no-daemon

echo "🚀 Build do projeto (Server e JS Development)..."
# Usamos jsBrowserDevelopmentExecutableDistribution por ser muito mais rápido e leve
./gradlew :server:installDist :composeApp:jsBrowserDevelopmentExecutableDistribution -Pandroid.useAndroidX=true --no-daemon

echo "✅ Build concluído!"

# Limpamos e criamos a estrutura de deploy
rm -rf deploy
mkdir -p deploy/server deploy/web

# 1. Copiar Servidor
SERVER_INSTALL_DIR=$(find server/build/install -maxdepth 1 -mindepth 1 -type d | head -n 1)
if [ -n "$SERVER_INSTALL_DIR" ]; then
    cp -R "$SERVER_INSTALL_DIR"/. deploy/server/
fi

# 2. Copiar Web (Usando a pasta Development)
JS_DIST_DIR="composeApp/build/dist/js/developmentExecutable"
if [ -d "$JS_DIST_DIR" ]; then
    echo "📦 Copiando binários JS (Dev)..."
    cp -R "$JS_DIST_DIR"/. deploy/web/
else
    echo "❌ ERRO: Binários JS não encontrados em $JS_DIST_DIR"
    exit 1
fi

# 3. FORÇAR index.html manual
echo "📄 Aplicando index.html..."
cp -f composeApp/src/webMain/resources/index.html deploy/web/index.html

echo "🐳 Subindo os containers..."
docker-compose up --build -d

echo "--------------------------------------------------------"
echo "✨ Sistema online (MODO DEV)!"
echo "🌐 Web: http://localhost:8081"
echo "🖥️  Server: http://localhost:8080"
echo "--------------------------------------------------------"

echo "📝 Escutando logs do servidor..."
docker-compose logs -f server
