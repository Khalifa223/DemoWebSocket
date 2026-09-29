// ----- Éléments de la page -----
const senderInput   = document.getElementById('sender');
const connectBtn    = document.getElementById('connectBtn');
const disconnectBtn = document.getElementById('disconnectBtn');
const statusLabel   = document.getElementById('status');
const messageForm   = document.getElementById('messageForm');
const messageInput  = document.getElementById('messageInput');
const sendBtn       = document.getElementById('sendBtn');
const messagesList  = document.getElementById('messages');
const logList       = document.getElementById('log');

// ----- 1. Création du client STOMP (aucune connexion n'est encore ouverte) -----
const wsProtocol = location.protocol === 'https:' ? 'wss:' : 'ws:';

const stompClient = new StompJs.Client({
	brokerURL: `${wsProtocol}//${location.host}/ws-chat`, // ex. ws://localhost:8080/ws-chat
	reconnectDelay: 0,                                    // pas de reconnexion automatique
	debug: (text) => console.log('[STOMP]', text),        // trames visibles dans F12 > Console
});

// ----- 2. Réactions aux événements de la connexion -----
stompClient.onConnect = (frame) => {
	// Le serveur a répondu CONNECTED : on peut envoyer des messages
	setConnected(true);
	log(`Connecté (STOMP ${frame.headers['version']})`);

	// Abonnement : chaque message publié sur /topic/messages arrive dans cette fonction
	stompClient.subscribe('/topic/messages', (stompMessage) => {
		const message = JSON.parse(stompMessage.body); // texte JSON -> objet JS
		showMessage(message);
	});
	log('Abonné à /topic/messages');
};

stompClient.onWebSocketClose = () => {
	setConnected(false);
	log('Connexion fermée');
};

stompClient.onWebSocketError = () => {
	log('Erreur WebSocket : le serveur est-il démarré ?');
};

stompClient.onStompError = (frame) => {
	log(`Erreur STOMP : ${frame.headers['message']}`);
};

// ----- 3. Déclenchement de la connexion / déconnexion -----
connectBtn.addEventListener('click', () => {
	connectBtn.disabled = true;
	log(`Connexion à ${stompClient.brokerURL}…`);
	stompClient.activate(); // ouvre le WebSocket (handshake) puis envoie la trame CONNECT
});

disconnectBtn.addEventListener('click', () => {
	stompClient.deactivate(); // envoie DISCONNECT puis ferme le WebSocket
});

// ----- 4. Envoi d'un message (clic sur "Envoyer" ou touche Entrée) -----
messageForm.addEventListener('submit', (event) => {
	event.preventDefault(); // empêche le formulaire de recharger la page

	const content = messageInput.value.trim();
	if (!content) {
		return;
	}
	const message = {
		sender: currentSender(),
		content: content,
	};

	stompClient.publish({
		destination: '/app/chat',                        // -> @MessageMapping("/chat") côté Spring
		headers: { 'content-type': 'application/json' },
		body: JSON.stringify(message),                   // l'objet JS devient du texte JSON
	});

	log(`Envoyé : ${JSON.stringify(message)}`);
	messageInput.value = '';
	messageInput.focus();
});

// ----- Utilitaires d'affichage -----
function currentSender() {
	return senderInput.value.trim() || 'Anonyme';
}

function showMessage(message) {
	const item = document.createElement('li');
	item.className = 'message-wrapper';

	if (message.sender === currentSender()) {
		item.classList.add('mine');
	} else {
		item.classList.add('other');
	}

	const senderName = document.createElement('div');
	senderName.className = 'sender-name';
	senderName.textContent = message.sender;

	const bubble = document.createElement('div');
	bubble.className = 'message-bubble';
	bubble.textContent = message.content;

	item.append(senderName, bubble);
	messagesList.append(item);
	
	// Utilisation du conteneur parent pour scroller (car ul prend la taille du parent)
	const chatBody = document.getElementById('chatBody');
	if(chatBody) {
		chatBody.scrollTop = chatBody.scrollHeight;
	} else {
		messagesList.scrollTop = messagesList.scrollHeight;
	}
}

function setConnected(connected) {
	senderInput.disabled = connected; // le pseudo est figé pendant la connexion
	connectBtn.disabled = connected;
	disconnectBtn.disabled = !connected;
	messageInput.disabled = !connected;
	sendBtn.disabled = !connected;
	
	statusLabel.textContent = connected ? 'Connecté' : 'Déconnecté';
	statusLabel.className = connected ? 'status-text on' : 'status-text off';
	
	const statusDot = document.getElementById('statusDot');
	if(statusDot) {
		statusDot.className = connected ? 'status-dot on' : 'status-dot off';
	}

	if (connected) {
		messageInput.focus();
	}
}

function log(text) {
	const item = document.createElement('li');
	item.textContent = `${new Date().toLocaleTimeString()}  ${text}`; // textContent : pas d'injection HTML
	logList.append(item);
}
