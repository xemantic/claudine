// WebSocket connection
let ws;

// Initialize WebSocket connection
function initWebSocket() {

    // Create WebSocket connection
    ws = new WebSocket(`${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws`);

    // Connection opened
    ws.addEventListener('open', function (event) {
        updateStatus('Connected');
        console.log('Connected to WebSocket server');
    });

    // Listen for messages
    ws.addEventListener('message', function (event) {
        console.log('Message from server:', event.data);
        displayMessage('Server: ' + event.data);
    });

    // Connection closed
    ws.addEventListener('close', function (event) {
        updateStatus('Disconnected');
        console.log('Disconnected from WebSocket server');
        // Try to reconnect after 5 seconds
        setTimeout(initWebSocket, 5000);
    });

    // Connection error
    ws.addEventListener('error', function (event) {
        updateStatus('Error');
        console.error('WebSocket error:', event);
    });
}

// Update connection status
function updateStatus(status) {
    document.getElementById('status').textContent = 'Status: ' + status;
}

// Display message in the messages div
function displayMessage(message) {
    const messagesDiv = document.getElementById('messages');
    const messageElement = document.createElement('div');
    messageElement.textContent = message;
    messagesDiv.appendChild(messageElement);
    // Scroll to bottom
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

// Send message to server
function sendMessage() {
    const input = document.getElementById('messageInput');
    const message = input.value;

    if (message && ws.readyState === WebSocket.OPEN) {
        ws.send(message);
        displayMessage('You: ' + message);
        input.value = '';
    }
}

// Initialize connection when page loads
document.addEventListener('DOMContentLoaded', function() {
    initWebSocket();
});