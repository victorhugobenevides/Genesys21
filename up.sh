#!/bin/bash

set -e
export NODE_OPTIONS="--max-old-space-size=4096"

echo "🧹 Limpando Docker (Caches e Snapshots)..."
docker builder prune -f
docker image prune -f

echo "🛑 Parando containers..."
docker-compose down --remove-orphans

echo "🧹 Limpando caches e corrigindo Yarn..."
rm -rf kotlin-js-store
rm -f yarn.lock
./gradlew clean --no-daemon

echo "🔄 Atualizando Yarn Lock..."
./gradlew :kotlinUpgradeYarnLock --no-daemon

echo "🧪 Executando todos os testes unitários..."
./gradlew :composeApp:testDebugUnitTest :server:test :shared:testDebugUnitTest --no-daemon

echo "📊 Gerando relatórios de cobertura Jacoco..."
./gradlew :composeApp:jacocoTestReport :server:jacocoServerTestReport :shared:jacocoSharedTestReport --no-daemon

echo "🚀 Build do projeto (Server e WasmJS)..."
./gradlew :server:installDist :composeApp:wasmJsBrowserDevelopmentExecutableDistribution -Pandroid.useAndroidX=true --no-daemon

echo "✅ Build concluído!"

rm -rf deploy
mkdir -p deploy/server deploy/web

# 1. Copiar Servidor e Credenciais
SERVER_INSTALL_DIR=$(find server/build/install -maxdepth 1 -mindepth 1 -type d | head -n 1)
if [ -n "$SERVER_INSTALL_DIR" ]; then
    cp -R "$SERVER_INSTALL_DIR"/. deploy/server/
    
    if [ -f "server/.env" ]; then
        echo "🔑 Copiando arquivo .env..."
        cp "server/.env" deploy/server/.env
    fi

    FIREBASE_JSON=$(find . -name "genesys21-32035-firebase-adminsdk-*.json" | head -n 1)
    if [ -n "$FIREBASE_JSON" ]; then
        echo "🔑 Copiando credenciais Firebase: $FIREBASE_JSON"
        cp "$FIREBASE_JSON" deploy/server/firebase-adminsdk.json
    fi
fi

# 2. Copiar Web (WasmJS)
find composeApp/build/dist/wasmJs/developmentExecutable -type f \( \
    -name "*.js" -o \
    -name "*.wasm" -o \
    -name "*.html" -o \
    -name "*.css" -o \
    -name "*.mjs" -o \
    -name "*.map" \
\) -exec cp -f {} deploy/web/ \;

# 3. Gerar firebase-bridge.js
echo "📦 Gerando firebase-bridge.js..."
cat <<EOF > deploy/web/firebase-bridge.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";

const firebaseConfig = {
    apiKey: "AIzaSyCq22tklAK0iQd4jWDINkJZAS9-I_-dLSY",
    authDomain: "genesys21-32035.firebaseapp.com",
    projectId: "genesys21-32035",
    storageBucket: "genesys21-32035.firebasestorage.app",
    appId: "1:674755208954:web:26e7b20a54f9ceb0dc4b43"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

globalThis.FirebaseAuthBridge = {
    ready: false,
    signIn: async (email, pass) => {
        const userCredential = await signInWithEmailAndPassword(auth, email, pass);
        return await userCredential.user.getIdToken();
    },
    signOut: async () => { await signOut(auth); },
    getCurrentUserToken: () => new Promise((resolve) => {
        const unsubscribe = onAuthStateChanged(auth, (user) => {
            unsubscribe();
            if (user) user.getIdToken().then(token => resolve(token)).catch(() => resolve(null));
            else resolve(null);
        });
    })
};

globalThis.FirebaseAuthBridge.ready = true;
console.log("BRIDGE: Firebase Bridge is READY.");
EOF

# 4. Gerar index.html
echo "📄 Gerando index.html..."
cat <<EOF > deploy/web/index.html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
    <title>Genesys21</title>
    <script type="module" src="/firebase-bridge.js"></script>
    <style>
        html, body { width: 100%; height: 100%; margin: 0; padding: 0; overflow: hidden; background-color: #F2F2F7; }
        #ComposeTarget { width: 100%; height: 100%; }
    </style>
</head>
<body>
    <canvas id="ComposeTarget"></canvas>
    <script type="module" src="/composeApp.js"></script>
</body>
</html>
EOF

chmod -R 755 deploy/web
docker-compose up --build -d

echo "--------------------------------------------------------"
echo "✨ Sistema online (WASM)!"
echo "🌐 Web: http://localhost"
echo "--------------------------------------------------------"
echo "📊 Relatórios de Cobertura (Jacoco):"
echo "📱 App: file://$(pwd)/composeApp/jacoco-reports/html/index.html"
echo "🖥️ Server: file://$(pwd)/server/jacoco-reports/html/index.html"
echo "📦 Shared: file://$(pwd)/shared/jacoco-reports/html/index.html"
echo "--------------------------------------------------------"
