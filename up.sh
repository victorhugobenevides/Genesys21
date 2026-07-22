#!/bin/bash

set -e
export NODE_OPTIONS="--max-old-space-size=4096"

echo "🧹 Limpando Docker (Caches e Snapshots)..."
docker builder prune -f
docker image prune -f

echo "🛑 Parando containers..."
docker compose down -v --remove-orphans || docker-compose down -v --remove-orphans

echo "🧹 Limpando caches e banco de dados..."
rm -rf kotlin-js-store
rm -f yarn.lock
rm -f data/genesys21.db*
./gradlew clean --no-daemon

echo "🚀 Build do projeto (Server e WasmJS)..."
./gradlew :server:installDist :composeApp:wasmJsBrowserDistribution -Pandroid.useAndroidX=true --no-daemon

echo "✅ Build concluído!"

rm -rf deploy
mkdir -p deploy/server deploy/web

# 1. Copiar Servidor
SERVER_INSTALL_DIR=$(find server/build/install -maxdepth 1 -mindepth 1 -type d | head -n 1)
if [ -n "$SERVER_INSTALL_DIR" ]; then
    cp -R "$SERVER_INSTALL_DIR"/. deploy/server/
    FIREBASE_JSON=$(find . -name "genesys21-32035-firebase-adminsdk-*.json" | head -n 1)
    if [ -n "$FIREBASE_JSON" ]; then cp "$FIREBASE_JSON" deploy/server/firebase-adminsdk.json; fi
fi

# 2. Copiar Web (WasmJS)
find composeApp/build/dist/wasmJs -type f \( \
    -name "*.js" -o -name "*.wasm" -o -name "*.html" -o -name "*.css" -o -name "*.mjs" -o -name "*.map" \
\) -exec cp -f {} deploy/web/ \;

# 3. Gerar firebase-bridge.js Corrigido
cat <<EOF > deploy/web/firebase-bridge.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword, createUserWithEmailAndPassword, signInWithPopup, GoogleAuthProvider, signOut, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";

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

window.firebaseSignUp = async (email, pass) => {
    const userCredential = await createUserWithEmailAndPassword(auth, email, pass);
    return await userCredential.user.getIdToken();
};

window.firebaseSignInGoogle = async () => {
    const provider = new GoogleAuthProvider();
    // Força a seleção de conta para uma experiência mais unificada
    provider.setCustomParameters({ prompt: 'select_account' });

    try {
        const result = await signInWithPopup(auth, provider);
        return await result.user.getIdToken();
    } catch (error) {
        if (error.code === 'auth/account-exists-with-different-credential') {
            throw new Error("ACCOUNT_EXISTS_PASSWORD");
        }
        throw error;
    }
};

window.firebaseGetToken = async () => {
    return auth.currentUser ? await auth.currentUser.getIdToken() : null;
};

window.firebaseGetUserId = async () => {
    return auth.currentUser ? auth.currentUser.uid : null;
};

window.firebaseSignOut = () => signOut(auth);
EOF

echo "🚀 Gerando Relatórios de Testes e Snapshots..."
./gradlew :screenshot-tests:testDebugUnitTest :shared:testDebugUnitTest :composeApp:testDebugUnitTest :server:test --continue || true

# 4. Copiar Relatórios de Qualidade (Para o Showcase)
echo "📂 Organizando relatórios de qualidade..."
mkdir -p deploy/web/reports/paparazzi
mkdir -p deploy/web/reports/shared
mkdir -p deploy/web/reports/app
mkdir -p deploy/web/reports/server
mkdir -p deploy/web/reports/coverage/app
mkdir -p deploy/web/reports/coverage/shared
mkdir -p deploy/web/reports/coverage/server

# Paparazzi - Copiando de forma robusta
PAPARAZZI_DIR=$(find screenshot-tests/build/reports/paparazzi -name "index.html" 2>/dev/null | head -n 1 | xargs dirname)
if [ -n "$PAPARAZZI_DIR" ]; then
    cp -R "$PAPARAZZI_DIR"/* deploy/web/reports/paparazzi/
    echo "✅ Relatório Paparazzi copiado."
fi

# Shared Tests
if [ -d "shared/build/reports/tests/testDebugUnitTest" ]; then
    cp -R shared/build/reports/tests/testDebugUnitTest/* deploy/web/reports/shared/
    echo "✅ Relatório Shared Tests copiado."
fi

# App Tests
if [ -d "composeApp/build/reports/tests/testDebugUnitTest" ]; then
    cp -R composeApp/build/reports/tests/testDebugUnitTest/* deploy/web/reports/app/
    echo "✅ Relatório App Tests copiado."
fi

# Server Tests
if [ -d "server/build/reports/tests/test" ]; then
    cp -R server/build/reports/tests/test/* deploy/web/reports/server/
    echo "✅ Relatório Server Tests copiado."
fi

# Coverage - Consolidando múltiplos relatórios
echo "📊 Coletando relatórios de cobertura (Jacoco)..."
if [ -d "composeApp/jacoco-reports/html" ]; then
    cp -R composeApp/jacoco-reports/html/. deploy/web/reports/coverage/app/
    echo "✅ Cobertura App copiada."
fi
if [ -d "shared/jacoco-reports/html" ]; then
    cp -R shared/jacoco-reports/html/. deploy/web/reports/coverage/shared/
    echo "✅ Cobertura Shared copiada."
fi
if [ -d "server/jacoco-reports/html" ]; then
    cp -R server/jacoco-reports/html/. deploy/web/reports/coverage/server/
    echo "✅ Cobertura Server copiada."
fi

# 5. Gerar index.html
cat <<EOF > deploy/web/index.html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <base href="/">
    <title>Genesys21</title>
    <script type="module" src="firebase-bridge.js"></script>
    <style>
        html, body { width: 100%; height: 100%; margin: 0; padding: 0; overflow: hidden; background-color: #F2F2F7; }
        #ComposeTarget { width: 100%; height: 100%; }
    </style>
</head>
<body>
    <div id="ComposeTarget"></div>
    <script type="module" src="composeApp.js"></script>
</body>
</html>
EOF

chmod -R 755 deploy/web
docker compose up --build -d || docker-compose up --build -d

echo "--------------------------------------------------------"
echo "✨ Sistema online (WASM)!"
echo "🌐 Web: http://localhost"
echo "--------------------------------------------------------"

# Iniciar ngrok em segundo plano para o backend (8080) e frontend (80)
echo "🚀 Iniciando ngrok para API (8080) e Web (80)..."
ngrok start --all --config "/Users/victorben/Library/Application Support/ngrok/ngrok.yml" --log=stdout > ngrok.log &
echo "✅ ngrok rodando (logs em ngrok.log)"




