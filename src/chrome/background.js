// Listen for connection attempts from popup
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action === "connect") {
        // Create WebSocket connection
        const ws = new WebSocket('ws://localhost:8080/claudine');

        ws.onopen = () => {
            console.log('Connected to WebSocket');
            chrome.runtime.sendMessage({status: 'connected'});
        };

        ws.onclose = () => {
            console.log('Disconnected from WebSocket');
            chrome.runtime.sendMessage({status: 'disconnected'});
        };

        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            chrome.runtime.sendMessage({status: 'error', error: error.message});
        };
    }
});
