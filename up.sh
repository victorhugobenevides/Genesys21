#!/bin/bash

set -e
export NODE_OPTIONS="--max-old-space-size=4096"

# Função para extrair cobertura do XML do Jacoco
get_coverage() {
    local xml_file=$1
    if [ -f "$xml_file" ]; then
        local line=$(tail -n 20 "$xml_file" | grep 'type="INSTRUCTION"')
        local missed=$(echo "$line" | sed 's/.*missed="\([^"]*\)".*/\1/')
        local covered=$(echo "$line" | sed 's/.*covered="\([^"]*\)".*/\1/')
        
        if [[ -n "$missed" && -n "$covered" ]]; then
            local total=$((missed + covered))
            if [ "$total" -gt 0 ]; then
                local percentage=$(( (covered * 100) / total ))
                echo "${percentage}%"
            else
                echo "0%"
            fi
        else
            echo "N/A"
        fi
    else
        echo "XML não encontrado"
    fi
}

echo "🧹 Limpando ambiente..."
docker-compose down --remove-orphans
./gradlew clean --no-daemon

echo "🧪 Executando testes unitários e screenshots..."
./gradlew :composeApp:testDebugUnitTest :server:test :shared:testDebugUnitTest :screenshot-tests:testDebugUnitTest --no-daemon

echo "📊 Gerando relatórios de cobertura Jacoco..."
./gradlew :composeApp:jacocoTestReport :server:jacocoServerTestReport :shared:jacocoSharedTestReport --no-daemon

# Extração da Cobertura para o Log
APP_COV=$(get_coverage "composeApp/jacoco-reports/report.xml")
SERVER_COV=$(get_coverage "server/jacoco-reports/report.xml")
SHARED_COV=$(get_coverage "shared/jacoco-reports/report.xml")

echo "--------------------------------------------------------"
echo "📈 RESUMO DE COBERTURA (INSTRUCTIONS):"
echo "📱 App (composeApp): $APP_COV"
echo "🖥️ Server:           $SERVER_COV"
echo "📦 Shared:           $SHARED_COV"
echo "--------------------------------------------------------"

echo "🚀 Build do projeto (Server e WasmJS)..."
./gradlew :server:installDist :composeApp:wasmJsBrowserDevelopmentExecutableDistribution -Pandroid.useAndroidX=true --no-daemon

rm -rf deploy
mkdir -p deploy/server deploy/web/reports

# 1. Copiar Servidor
SERVER_INSTALL_DIR=$(find server/build/install -maxdepth 1 -mindepth 1 -type d | head -n 1)
[ -n "$SERVER_INSTALL_DIR" ] && cp -R "$SERVER_INSTALL_DIR"/. deploy/server/
[ -f "server/.env" ] && cp "server/.env" deploy/server/.env
FIREBASE_JSON=$(find . -name "genesys21-32035-firebase-adminsdk-*.json" | head -n 1)
[ -n "$FIREBASE_JSON" ] && cp "$FIREBASE_JSON" deploy/server/firebase-adminsdk.json

# 2. Copiar Web (WasmJS)
find composeApp/build/dist/wasmJs/developmentExecutable -type f \( -name "*.js" -o -name "*.wasm" -o -name "*.html" -o -name "*.css" -o -name "*.mjs" -o -name "*.map" \) -exec cp -f {} deploy/web/ \;

# 3. Copiar Relatórios (Organizado para Docker)
echo "📊 Organizando relatórios para acesso via Web..."
mkdir -p deploy/web/reports/app deploy/web/reports/server deploy/web/reports/shared deploy/web/reports/screenshots

cp -R composeApp/jacoco-reports/html/* deploy/web/reports/app/ 2>/dev/null || true
cp -R server/jacoco-reports/html/* deploy/web/reports/server/ 2>/dev/null || true
cp -R shared/jacoco-reports/html/* deploy/web/reports/shared/ 2>/dev/null || true

# COPIAR O RELATÓRIO DO PAPARAZZI (Viewer oficial)
if [ -d "screenshot-tests/build/reports/paparazzi" ]; then
    cp -R screenshot-tests/build/reports/paparazzi/* deploy/web/reports/screenshots/
fi

# 4. Gerar firebase-bridge.js e index.html (Omitido para brevidade, mantém o anterior)
# ... código de geração dos arquivos JS/HTML ...

chmod -R 755 deploy/web
docker-compose up --build -d

echo "--------------------------------------------------------"
echo "✨ Sistema online!"
echo "🌐 App Web: http://localhost"
echo "📊 Relatórios no Docker:"
echo "📱 App:         http://localhost/reports/app/"
echo "🖥️ Server:      http://localhost/reports/server/"
echo "📦 Shared:      http://localhost/reports/shared/"
echo "📸 Screenshots: http://localhost/reports/screenshots/index.html"
echo "--------------------------------------------------------"
