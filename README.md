# Live Sync Pad 📝⚡

Live Sync Pad is a real-time, collaborative text editor built with **Spring Boot WebSockets** and the **Monaco Editor** (the engine behind VS Code). It allows multiple users to simultaneously edit a shared document with live visual tracking of remote cursors, dynamic user presence, synchronized dual-pane scrolling, and instant Markdown-to-HTML rendering.

## 🚀 Features

*   **Real-Time Collaborative Editing:** Document synchronization across multiple clients using a Spring Boot STOMP WebSocket broker.
*   **Live Markdown Rendering:** Instantly parses and renders Markdown syntax into formatted HTML using the Marked.js library.
*   **Live Remote Cursors:** Visual cursor tracking utilizing the Monaco Decorations API. Users are dynamically assigned unique colors and floating name tags based on a mathematical hash of their username.
*   **Thread-Safe Presence Management:** Robust backend session tracking using a `ConcurrentHashMap` to accurately monitor active viewers by intercepting STOMP connection headers.
*   **Ghost Cursor Cleanup:** Automated event listeners that instantly broadcast targeted teardown commands when a user's TCP connection drops, keeping the UI clean of disconnected client artifacts.
*   **Synchronized Scrolling:** Proportional scroll-syncing between the Monaco editor canvas and the HTML preview pane for a seamless writing experience.
*   **File Persistence:** Read and write capability directly to the local server file system.

## 🛠️ Tech Stack

**Backend:**
*   Java / Spring Boot
*   Spring WebSockets (STOMP Messaging Protocol)
*   Concurrent Data Structures (`ConcurrentHashMap`)

**Frontend:**
*   JavaScript (Vanilla/ES6)
*   Monaco Editor API (Microsoft)
*   Marked.js (Markdown Compiler)
*   HTML5 & CSS3 (Flexbox architecture)
*   Stomp.js

## 🧠 Architecture & Technical Highlights

This project demonstrates practical solutions for distributed client state and real-time UI synchronization:

1.  **Header-Based Session Tracking:** The backend intercepts `SessionConnectEvent` payloads to extract native headers, mapping STOMP Session IDs to usernames in a thread-safe `ConcurrentHashMap`. This guarantees accurate presence data upon connection and disconnection.
2.  **Stateful UI Rendering:** The frontend utilizes a `Map` structure to track active `monaco.Range` coordinates for all connected peers. The engine isolates and redraws the active coordinate sets using `remoteCursors.set()`, allowing simultaneous multi-cursor rendering without flickering or visual collision.
3.  **Event-Driven Data Injection:** Incoming WebSocket payloads are decoupled from the user's local keystrokes. An `isUpdatingFromServer` locking flag prevents the Monaco editor from echoing remote changes back to the server, successfully avoiding infinite broadcast loops.

## ⚙️ Getting Started

### Prerequisites
*   Java 17 or higher
*   Maven

### Installation & Execution

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/MounikaBoini/live-sync-pad.git](https://github.com/MounikaBoini/live-sync-pad.git)
    cd live-sync-pad
    ```
2.  **Build and run the Spring Boot application**
    ```bash
    ./mvnw spring-boot:run
    ```
3.  **Open the application**
    Navigate to `http://localhost:8080` in your web browser.
    *(Open multiple tabs to test the multiplayer features!)*