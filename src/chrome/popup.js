document.getElementById('connect').addEventListener('click', () => {
    console.log('clicked');
    chrome.runtime.sendMessage({action: 'connect'});
});

// Listen for status updates from background script
chrome.runtime.onMessage.addListener((message) => {
    console.log(message);
    const statusDiv = document.getElementById('status');
    if (message.status === 'connected') {
        statusDiv.textContent = 'Connected';
        statusDiv.style.color = 'green';
    } else if (message.status === 'disconnected') {
        statusDiv.textContent = 'Disconnected';
        statusDiv.style.color = 'red';
    } else if (message.status === 'error') {
        statusDiv.textContent = `Error: ${message.error}`;
        statusDiv.style.color = 'red';
    }
});