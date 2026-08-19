// Genesys21 Web Interop Bridge (Firebase + Stripe + Connect)

const firebaseConfig = {
    apiKey: "AIzaSyCq22tklAK0iQd4jWDINkJZAS9-I_-dLSY",
    authDomain: "genesys21-32035.firebaseapp.com",
    projectId: "genesys21-32035",
    storageBucket: "genesys21-32035.firebasestorage.app",
    appId: "1:674755208954:web:26e7b20a54f9ceb0dc4b43"
};

if (!firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
}
const auth = firebase.auth();
auth.setPersistence(firebase.auth.Auth.Persistence.LOCAL);

// Garantindo que o estado inicial do Firebase seja carregado
const authReadyPromise = new Promise((resolve) => {
    const unsubscribe = auth.onAuthStateChanged((user) => {
        console.log("BRIDGE: Auth state changed, user:", user ? user.uid : "null");
        resolve(user);
    });
});

// --- FIREBASE BRIDGE ---

window.firebaseSignIn = (email, pass) => auth.signInWithEmailAndPassword(email, pass).then(u => u.user.getIdToken());

window.firebaseSignUp = (email, pass) => auth.createUserWithEmailAndPassword(email, pass).then(u => u.user.getIdToken());

window.firebaseSignInGoogle = () => {
    isRedirecting = true;
    const provider = new firebase.auth.GoogleAuthProvider();
    console.log("BRIDGE: Iniciando Login Redirect...");
    return auth.signInWithRedirect(provider).then(() => "REDIRECTING");
};

// Processa o resultado do redirecionamento ao carregar a página
auth.getRedirectResult().then((result) => {
    if (result && result.user) {
        console.log("BRIDGE: Sucesso no retorno do Google Redirect:", result.user.email);
    }
}).catch(err => {
    console.error("BRIDGE: Erro no retorno do Redirect", err);
    isRedirecting = false;
});

window.firebaseGetToken = () => authReadyPromise.then(user => user ? user.getIdToken() : null);
window.firebaseGetUserId = () => authReadyPromise.then(user => user ? user.uid : null);

window.firebaseGetUserEmail = () => {
    const user = auth.currentUser;
    return Promise.resolve(user ? user.email : null);
};

window.firebaseGetUserName = () => {
    const user = auth.currentUser;
    return Promise.resolve(user ? user.displayName : null);
};

window.firebaseSignOut = () => {
    oneTapInitialized = false;
    return auth.signOut();
};

window.firebaseDeleteUser = () => {
    const user = auth.currentUser;
    if (user) {
        return user.delete();
    }
    return Promise.resolve();
};

window.firebaseOnAuthChanged = (callback) => {
    auth.onAuthStateChanged((user) => {
        callback(user ? user.uid : null);
    });
};

// --- GOOGLE ONE TAP ---

let oneTapInitialized = false;
let isRedirecting = false;

window.firebaseInitializeOneTap = () => {
    if (oneTapInitialized || auth.currentUser || isRedirecting || typeof google === 'undefined') return;

    oneTapInitialized = true;
    console.log("BRIDGE: Iniciando One Tap...");

    google.accounts.id.initialize({
        client_id: "674755208954-6ofmvlcn9birat7ako2banqc9ph1t74s.apps.googleusercontent.com",
        auto_select: true,
        callback: (response) => {
            console.log("BRIDGE: Resposta One Tap recebida");
            const credential = firebase.auth.GoogleAuthProvider.credential(response.credential);
            auth.signInWithCredential(credential)
                .then(() => console.log("BRIDGE: One Tap Login Sucesso"))
                .catch(err => {
                    oneTapInitialized = false;
                    console.error("BRIDGE: One Tap Login Erro", err);
                });
        }
    });

    google.accounts.id.prompt((notification) => {
        if (notification.isNotDisplayed() || notification.isSkippedMoment() || notification.isDismissedMoment()) {
            oneTapInitialized = false;
        }
    });
};

// --- STRIPE BRIDGE ---

let stripe;
let elements;

window.stripeInitialize = (publishableKey) => {
    console.log("BRIDGE: Initializing Stripe with key:", publishableKey.substring(0, 10) + "...");
    stripe = Stripe(publishableKey);
};

window.stripeMountPaymentElement = (clientSecret, appearanceJson, elementId) => {
    if (!stripe) return Promise.reject("Stripe not initialized");

    console.log("BRIDGE: Mounting Payment Element to:", elementId);
    const appearance = JSON.parse(appearanceJson);
    elements = stripe.elements({ clientSecret, appearance });

    const paymentElement = elements.create("payment");
    paymentElement.mount(`#${elementId}`);
    return Promise.resolve();
};

window.stripeConfirmPayment = (returnUrl) => {
    if (!stripe || !elements) return Promise.reject("Stripe not ready");

    console.log("BRIDGE: Confirming payment with return URL:", returnUrl);
    return stripe.confirmPayment({
        elements,
        confirmParams: {
            return_url: returnUrl,
        },
    });
};

// --- STRIPE CONNECT BRIDGE ---

let stripeConnectInstance;

window.stripeConnectInitialize = (publishableKey, clientSecret) => {
    console.log("BRIDGE: Initializing Stripe Connect...");
    stripeConnectInstance = window.StripeConnect.init({
        publishableKey: publishableKey,
        fetchClientSecret: () => Promise.resolve(clientSecret),
        appearance: {
            overlays: 'dialog',
            variables: {
                colorPrimary: '#007AFF',
            },
        },
    });
    return Promise.resolve();
};

window.stripeConnectMountComponent = (componentName, containerId) => {
    if (!stripeConnectInstance) return Promise.reject("Stripe Connect not initialized");

    console.log("BRIDGE: Mounting Connect component:", componentName, "to", containerId);
    const container = document.getElementById(containerId);
    if (!container) return Promise.reject("Container not found: " + containerId);

    const component = stripeConnectInstance.create(componentName);
    container.innerHTML = '';
    component.mount(container);
    return Promise.resolve();
};

console.log("BRIDGE: Web Interop Bridge loaded successfully.");
