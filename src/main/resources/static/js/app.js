// DOM Elements
const preview = document.getElementById('preview');
const statusText = document.getElementById('connection-status');
const saveBtn = document.getElementById('save-btn');
let stompClient = null;
// Ask for username immediately when the script loads
const currentUsername = prompt("Welcome to Live Sync Pad! Please enter your name:") || "Anonymous User";

// --- NEW: Color Generator ---
const userColors = ['#FF3B30', '#FF9500', '#4CD964', '#007AFF', '#5856D6', '#FF2D55', '#009688', '#E91E63'];

function getColorForUser(username) {
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }
    return userColors[Math.abs(hash) % userColors.length];
}
// --- Initialize Monaco Editor ---
const editor = monaco.editor.create(document.getElementById('editor-container'), {
    value: "Loading file...",
    language: "markdown",
    theme: "vs-light",
    automaticLayout: true
});

// Initialize the decorations tracker for remote cursors
const remoteCursors = editor.createDecorationsCollection([]);

function connect() {

    stompClient = Stomp.client('ws://localhost:8080/gs-guide-websocket');

    // Turn off debug logging in console (optional)
    stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        statusText.textContent = 'Connected';
        statusText.style.color = '#28a745';

        //subscribe to initial load channel
        stompClient.subscribe('/topic/loaded', function (message) {
            const initialContent = message.body;

            isUpdatingFromServer = true;
            editor.setValue(initialContent);
            isUpdatingFromServer = false;

            updatePreview(initialContent);
        });
        stompClient.subscribe('/topic/updates', function (message) {
            const updateData = JSON.parse(message.body);

            // Prevent echoing our own changes back onto ourselves
            if (updateData.username !== currentUsername) {
                // Update text content (only if it actually changed)
                if (editor.getValue() !== updateData.content) {
                    isUpdatingFromServer = true;
                    editor.setValue(updateData.content);
                    isUpdatingFromServer = false;

                    updatePreview(updateData.content);
                }

                // Draw the remote cursor at the specified coordinates
                if (updateData.cursorLine && updateData.cursorColumn) {

                    // Strip spaces to make a safe CSS class name (e.g., "John Doe" -> "JohnDoe")
                    let safeUsername = updateData.username.replace(/[^a-zA-Z0-9]/g, '');
                    let styleId = 'style-' + safeUsername;

                    // Dynamically create a unique color rule for this specific user
                    if (!document.getElementById(styleId)) {
                        const userColor = getColorForUser(updateData.username);
                        const style = document.createElement('style');
                        style.id = styleId;

                        style.innerHTML = `
                        .cursor-${safeUsername} {
                            border-left: 2px solid ${userColor} !important;
                        }
                        /* display: inline-block and position: absolute prevent the tag from pushing file text apart */
                        .cursor-name-${safeUsername} {
                            position: absolute;
                            display: inline-block;
                        }
                        .cursor-name-${safeUsername}::after {
                            content: "${updateData.username}";
                            background-color: ${userColor};
                            color: white;
                            font-size: 10px;
                            font-weight: bold;
                            padding: 2px 6px;
                            border-radius: 4px;
                            border-bottom-left-radius: 0px; /* Makes it look like a chat bubble */
                            box-shadow: 0 2px 4px rgba(0,0,0,0.3); /* Strong shadow so it looks like UI, not text */
                            position: absolute;
                            top: -22px;
                            left: 0;
                            white-space: nowrap;
                            pointer-events: none;
                            z-index: 100;
                        }
                    `;
                        document.head.appendChild(style);
                    }

                    // Apply the new dynamic classes to Monaco
                    remoteCursors.set([{
                        range: new monaco.Range(
                            updateData.cursorLine,
                            updateData.cursorColumn,
                            updateData.cursorLine,
                            updateData.cursorColumn
                        ),
                        options: {
                            className: `remote-cursor cursor-${safeUsername}`,
                            hoverMessage: { value: updateData.username },
                            beforeContentClassName: `cursor-name-${safeUsername}`
                        }
                    }]);
                }
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
// Handle Content Changes (Typing)
editor.onDidChangeModelContent((e) => {
    // Only broadcast if the user is typing, NOT if the server just updated the editor
    if (!isUpdatingFromServer && stompClient !== null && stompClient.connected) {
        const content = editor.getValue();
        
        // Update local preview instantly for a snappy UI
        updatePreview(content);
        
        const position = editor.getPosition();
        const payload = {
            username: currentUsername,
            content: content,
            cursorLine: position ? position.lineNumber : 1,
            cursorColumn: position ? position.column : 1
        };
        
        stompClient.send("/app/edit", {}, JSON.stringify(payload));
    }
});
// Handle Cursor Movement (Clicking or Arrow keys)
editor.onDidChangeCursorPosition((e) => {
    if (!isUpdatingFromServer && stompClient !== null && stompClient.connected) {
        const payload = {
            username: currentUsername,
            content: editor.getValue(), // Include content so we don't accidentally wipe it
            cursorLine: e.position.lineNumber,
            cursorColumn: e.position.column
        };
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
