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
	const sender = document.createElement('span');
	sender.className = 'sender';
	sender.textContent = `${message.sender} : `;
	item.append(sender, message.content); // une chaîne ajoutée par append() reste du texte : pas d'injection HTML
	if (message.sender === currentSender()) {
		item.classList.add('mine'); // nos propres messages, renvoyés par le serveur, en bleu
	}
	messagesList.append(item);
	messagesList.scrollTop = messagesList.scrollHeight; // défile jusqu'au dernier message
}

function setConnected(connected) {
	senderInput.disabled = connected; // le pseudo est figé pendant la connexion
	connectBtn.disabled = connected;
	disconnectBtn.disabled = !connected;
	messageInput.disabled = !connected;
	sendBtn.disabled = !connected;
	statusLabel.textContent = connected ? 'Connecté' : 'Déconnecté';
	statusLabel.className = connected ? 'status on' : 'status off';
	if (connected) {
		messageInput.focus();
	}
}

function log(text) {
	const item = document.createElement('li');
	item.textContent = `${new Date().toLocaleTimeString()}  ${text}`; // textContent : pas d'injection HTML
	logList.append(item);
}
