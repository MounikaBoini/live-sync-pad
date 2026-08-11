// DOM Elements
const editor = document.getElementById('editor');
const preview = document.getElementById('preview');
const statusText = document.getElementById('connection-status');
const saveBtn = document.getElementById('save-btn');
const typingIndicator = document.getElementById('typing-indicator');
let typingTimeout = null;
let stompClient = null;
// Ask for username immediately when the script loads
const currentUsername = prompt("Welcome to Live Sync Pad! Please enter your name:") || "Anonymous User";

function connect() {

    stompClient = Stomp.client('ws://localhost:8080/gs-guide-websocket');

    // Turn off debug logging in console (optional)
    stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        statusText.textContent = 'Connected';
        statusText.style.color = '#28a745';
        console.log('Connected: ' + frame);

        //subscribe to initial load channel
        stompClient.subscribe('/topic/loaded', function (message) {
            const initialContent = message.body;
            // Populate BOTH the editor and preview with the file data
            editor.value = initialContent;
            updatePreview(initialContent);
        });
        //subscribe to live preview channel
        stompClient.subscribe('/topic/updates', function (message) {
            // Parse the incoming JSON string back into a JavaScript object
            const updateData = JSON.parse(message.body);

            updatePreview(updateData.content);

            // If someone else is typing, show their name
            if (updateData.username !== currentUsername) {
                showTypingIndicator(updateData.username);
            }
        });
        //subscribe to get viewer count
        stompClient.subscribe('/topic/viewers', function (message) {
            const count = message.body;
            document.getElementById('viewer-count').textContent = count;
        });
        // Immediately ask the server for the file data now that we are connected
        stompClient.send("/app/load", {}, "");

        //immediately ask the server for active viewer count
        stompClient.send("/app/viewers", {}, "");
    }, function (error) {
        statusText.textContent = 'Disconnected';
        statusText.style.color = '#dc3545';
        // Attempt to reconnect after 5 seconds
        setTimeout(connect, 5000);
    });
}
// Handle Live Typing
editor.addEventListener('input', (e) => {
    const content = e.target.value;
    if (stompClient !== null && stompClient.connected) {

        // Create the JSON payload
        const payload = {
            username: currentUsername,
            content: content
        };

        // Convert to string and send
        stompClient.send("/app/edit", {}, JSON.stringify(payload));
    }
});

function showTypingIndicator(username) {
    typingIndicator.textContent = username + " is typing...";

    // Clear previous timeout if the user keeps typing
    clearTimeout(typingTimeout);

    // Hide the message after 2 seconds of inactivity
    typingTimeout = setTimeout(() => {
        typingIndicator.textContent = "";
    }, 2000);
}

// Listen for user edits and send to Spring Boot (@MessageMapping)
editor.addEventListener('input', (e) => {
    const content = e.target.value;

    if (stompClient !== null && stompClient.connected) {
        // 1. Create the object
        const payload = {
            username: currentUsername,
            content: content
        };

        // 2. Convert it to a JSON string before sending
        stompClient.send("/app/edit", {}, JSON.stringify(payload));
    }
});

// Handle Save Button Click
saveBtn.addEventListener('click', () => {
    if (stompClient !== null && stompClient.connected) {
        const currentContent = editor.value;
        // Send the complete content to the save endpoint
        stompClient.send("/app/save", {}, currentContent);

        // Brief visual feedback for the user
        const originalText = saveBtn.textContent;
        saveBtn.textContent = "Saved!";
        setTimeout(() => saveBtn.textContent = originalText, 2000);
    }
});
function updatePreview(content) {
    preview.textContent = content;
}

// Start connection when page loads
connect();
