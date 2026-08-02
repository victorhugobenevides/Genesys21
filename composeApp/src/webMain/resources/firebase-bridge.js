// Configuração do Firebase
const firebaseConfig = {
    apiKey: "AIzaSyCq22tklAK0iQd4jWDINkJZAS9-I_-dLSY",
    authDomain: "genesys21-32035.firebaseapp.com",
    projectId: "genesys21-32035",
    storageBucket: "genesys21-32035.firebasestorage.app",
    appId: "1:674755208954:web:26e7b20a54f9ceb0dc4b43"
};

// Inicialização (Compat Mode)
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();

// Funções expostas para o Kotlin (WASM)
window.firebaseSignIn = (email, pass) => {
    return auth.signInWithEmailAndPassword(email, pass)
        .then(userCredential => userCredential.user.getIdToken());
};

window.firebaseSignInGoogle = () => {
    const provider = new firebase.auth.GoogleAuthProvider();
    return auth.signInWithPopup(provider)
        .then(result => result.user.getIdToken())
        .catch(error => {
            if (error.code === 'auth/account-exists-with-different-credential') {
                throw new Error("ACCOUNT_EXISTS_PASSWORD");
            }
            throw error;
        });
};

window.firebaseGetToken = () => {
    if (auth.currentUser) {
        return auth.currentUser.getIdToken();
    }
    return Promise.resolve(null);
};

window.firebaseGetUserId = () => {
    if (auth.currentUser) {
        return Promise.resolve(auth.currentUser.uid);
    }
    return Promise.resolve(null);
};

window.firebaseGetUserEmail = () => {
    if (auth.currentUser) {
        return Promise.resolve(auth.currentUser.email);
    }
    return Promise.resolve(null);
};

window.firebaseGetUserName = () => {
    if (auth.currentUser) {
        return Promise.resolve(auth.currentUser.displayName);
    }
    return Promise.resolve(null);
};

window.firebaseSignOut = () => {
    oneTapInitialized = false;
    return auth.signOut();
};

// GOOGLE ONE TAP
let oneTapInitialized = false;
window.firebaseInitializeOneTap = () => {
    if (oneTapInitialized || auth.currentUser || typeof google === 'undefined') return;
    oneTapInitialized = true;
    console.log("WASM: Iniciando One Tap...");

    google.accounts.id.initialize({
        client_id: "674755208954-6ofmvlcn9birat7ako2banqc9ph1t74s.apps.googleusercontent.com",
        callback: (response) => {
            const credential = firebase.auth.GoogleAuthProvider.credential(response.credential);
            auth.signInWithCredential(credential)
                .then(() => console.log("WASM: One Tap Sucesso"))
                .catch(err => console.error("WASM: One Tap Erro", err));
        }
    });
    google.accounts.id.prompt();
};

console.log("WASM: Ponte Firebase (Compat) carregada com sucesso.");
