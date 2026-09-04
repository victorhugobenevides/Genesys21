#!/bin/bash

# up.sh - Script de Validação e Deploy Local (Simula Pipeline CI/CD)
set -e
export NODE_OPTIONS="--max-old-space-size=4096"

echo "🧹 Limpando ambiente..."
docker builder prune -f
docker image prune -f
rm -rf kotlin-js-store yarn.lock data/genesys21.db* deploy/
./gradlew clean --no-daemon

echo "🎨 1. Verificando Integridade Visual (Screenshot Tests)..."
# Simula o passo 'Verify Visual Integrity' da pipeline
./gradlew :screenshot-tests:verifyPaparazziDebug --no-daemon

echo "🧪 2. Rodando Testes Unitários e Cobertura (Jacoco)..."
# Simula o passo 'Run Unit Tests & Coverage' da pipeline
./gradlew :shared:testDebugUnitTest :composeApp:testDebugUnitTest :server:test --no-daemon
./gradlew :server:jacocoTestReport --no-daemon

echo "📦 3. Build de Produção (Server e WasmJS)..."
# Simula o passo 'Build Project' da pipeline (Production Distribution)
./gradlew :server:installDist :composeApp:wasmJsBrowserDistribution -Pandroid.useAndroidX=true --no-daemon

echo "📂 4. Organizando artefatos para Deploy Local..."
mkdir -p deploy/server deploy/web/reports/paparazzi deploy/web/reports/shared deploy/web/reports/app deploy/web/reports/server deploy/web/reports/coverage/app deploy/web/reports/coverage/shared deploy/web/reports/coverage/server

# Copiar Servidor
SERVER_INSTALL_DIR=$(find server/build/install -maxdepth 1 -mindepth 1 -type d | head -n 1)
if [ -n "$SERVER_INSTALL_DIR" ]; then
    cp -R "$SERVER_INSTALL_DIR"/. deploy/server/
    # Tenta encontrar o JSON do Firebase Admin para o servidor local
    FIREBASE_JSON=$(find . -name "genesys21-32035-firebase-adminsdk-*.json" | head -n 1)
    if [ -n "$FIREBASE_JSON" ]; then cp "$FIREBASE_JSON" deploy/server/firebase-adminsdk.json; fi
fi

# Copiar Web (WasmJS Production)
# Nota: Caminho corrigido para productionExecutable ou distribution
WEB_DIST_DIR=$(find composeApp/build/dist/wasmJs -name "productionExecutable" -o -name "distribution" | head -n 1)
if [ -n "$WEB_DIST_DIR" ]; then
    find "$WEB_DIST_DIR" -type f \( \
        -name "*.js" -o -name "*.wasm" -o -name "*.html" -o -name "*.css" -o -name "*.mjs" -o -name "*.map" \
    \) -exec cp -f {} deploy/web/ \;
fi

# Copiar Relatórios (Igual a Pipeline)
[ -d "screenshot-tests/build/reports/paparazzi/debug" ] && cp -R screenshot-tests/build/reports/paparazzi/debug/* deploy/web/reports/paparazzi/ || true
[ -d "shared/build/reports/tests/testDebugUnitTest" ] && cp -R shared/build/reports/tests/testDebugUnitTest/* deploy/web/reports/shared/ || true
[ -d "composeApp/build/reports/tests/testDebugUnitTest" ] && cp -R composeApp/build/reports/tests/testDebugUnitTest/* deploy/web/reports/app/ || true
[ -d "server/build/reports/tests/test" ] && cp -R server/build/reports/tests/test/* deploy/web/reports/server/ || true

# Copiar Cobertura
[ -d "composeApp/jacoco-reports/html" ] && cp -R composeApp/jacoco-reports/html/* deploy/web/reports/coverage/app/ || true
[ -d "shared/jacoco-reports/html" ] && cp -R shared/jacoco-reports/html/* deploy/web/reports/coverage/shared/ || true
[ -d "server/jacoco-reports/html" ] && cp -R server/jacoco-reports/html/* deploy/web/reports/coverage/server/ || true

# 5. Gerar ponte Firebase e index.html (Alinhado com a Pipeline)
cat <<EOF > deploy/web/firebase-bridge.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword, signOut, GoogleAuthProvider, signInWithPopup, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";

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

window.firebaseSignInGoogle = async () => {
    const provider = new GoogleAuthProvider();
    provider.setCustomParameters({ prompt: 'select_account' });
    const result = await signInWithPopup(auth, provider);
    return await result.user.getIdToken();
};

window.firebaseGetToken = async () => auth.currentUser ? await auth.currentUser.getIdToken() : null;
window.firebaseGetUserId = async () => auth.currentUser ? auth.currentUser.uid : null;
window.firebaseGetUserEmail = async () => auth.currentUser ? auth.currentUser.email : null;
window.firebaseGetUserName = async () => auth.currentUser ? auth.currentUser.displayName : null;
window.firebaseSignOut = async () => await signOut(auth);

window.firebaseOnAuthChanged = (callback) => {
    console.log("BRIDGE: Registering auth state listener...");
    onAuthStateChanged(auth, async (user) => {
        if (user) {
            console.log("BRIDGE: User detected:", user.uid, user.email);
            callback(user.uid);
        } else {
            console.log("BRIDGE: No user detected (null)");
            callback(null);
        }
    });
};
EOF

cat <<EOF > deploy/web/index.html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover, interactive-widget=resizes-content">
    <title>Genesys21</title>
    <link rel="preload" href="/composeApp.wasm" as="fetch" type="application/wasm" crossorigin>
    <script>if(!window.crypto.randomUUID){window.crypto.randomUUID=function(){return([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g,c=>(c^crypto.getRandomValues(new Uint8Array(1))[0]&15>>c/4).toString(16));};}</script>
    <script type="module" src="/firebase-bridge.js"></script>
    <style>
        html, body { width: 100%; height: 100%; margin: 0; padding: 0; overflow: hidden; background-color: #F2F2F7; position: fixed; left: 0; top: 0; touch-action: manipulation; }
        #ComposeTarget { width: 100%; height: 100%; }
        canvas { outline: none; width: 100% !important; height: 100% !important; display: block; }
    </style>
</head>
<body>
    <canvas id="ComposeTarget"></canvas>
    <script type="module" src="/composeApp.js"></script>
</body>
</html>
EOF

echo "🚀 Subindo Containers Localmente..."
chmod -R 755 deploy/web
docker compose down -v --remove-orphans || true
docker compose up --build -d

echo "--------------------------------------------------------"
echo "✨ Validação concluída e Sistema Online (Produção Local)!"
echo "🌐 Web: http://localhost"
echo "📂 Relatórios: deploy/web/reports/"
echo "--------------------------------------------------------"

# ngrok (opcional, mantido para facilidade)
if command -v ngrok &> /dev/null; then
    echo "🚀 Iniciando ngrok para API (8080) e Web (80)..."
    ngrok start --all --config "/Users/victorben/Library/Application Support/ngrok/ngrok.yml" --log=stdout > ngrok.log &
    echo "✅ ngrok rodando (logs em ngrok.log)"
fi

echo "📋 Seguindo logs do servidor..."
docker compose logs -f server
