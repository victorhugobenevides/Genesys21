#!/bin/bash

# Aborta o script em caso de erro
set -e

# Aumenta a memória disponível para o processo do Node/Webpack
export NODE_OPTIONS="--max-old-space-size=4096"

echo "🛑 Parando containers..."
docker-compose down -v --remove-orphans

echo "🧹 Limpando caches e corrigindo Yarn..."
rm -rf kotlin-js-store
rm -f yarn.lock
./gradlew clean --no-daemon

# Força a atualização do lockfile para evitar erro de sincronização
echo "🔄 Atualizando Yarn Lock..."
./gradlew :kotlinUpgradeYarnLock --no-daemon

echo "🚀 Build do projeto (Server e WasmJS)..."
./gradlew :server:installDist :composeApp:wasmJsBrowserDevelopmentExecutableDistribution -Pandroid.useAndroidX=true --no-daemon

echo "✅ Build concluído!"

# Limpamos e criamos a estrutura de deploy
rm -rf deploy
mkdir -p deploy/server deploy/web

# 1. Copiar Servidor
SERVER_INSTALL_DIR=$(find server/build/install -maxdepth 1 -mindepth 1 -type d | head -n 1)
if [ -n "$SERVER_INSTALL_DIR" ]; then
    cp -R "$SERVER_INSTALL_DIR"/. deploy/server/
fi

# 2. Copiar Web (WasmJS)
find composeApp/build/dist/wasmJs/developmentExecutable -type f \( -name "*.js" -o -name "*.wasm" -o -name "*.html" -o -name "*.css" -o -name "*.mjs" \) -exec cp -f {} deploy/web/ \;

# 3. Gerar firebase-bridge.js (Evita erro de CSP inline)
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

window.firebaseSignIn = async (email, pass) => {
    const userCredential = await signInWithEmailAndPassword(auth, email, pass);
    return await userCredential.user.getIdToken();
};

window.firebaseGetToken = async () => {
    return auth.currentUser ? await auth.currentUser.getIdToken() : null;
};

window.firebaseSignOut = async () => {
    await signOut(auth);
};
EOF

# 4. Gerar index.html limpo com caminhos ABSOLUTOS para suportar rotas como /p/ID
echo "📄 Gerando index.html..."
cat <<EOF > deploy/web/index.html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Genesys21</title>
    <!-- Uso de caminhos absolutos (/) para evitar 404 em sub-rotas -->
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

# 5. Ajustar permissões
chmod -R 755 deploy/web

echo "🐳 Subindo os containers..."
docker-compose up --build -d

echo "--------------------------------------------------------"
echo "✨ Sistema online (WASM)!"
echo "🌐 Web: http://localhost:8081"
echo "--------------------------------------------------------"

echo "📝 Logs do servidor..."
docker-compose logs -f server
