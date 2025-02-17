# Rock-Paper-Scissors 
### 🪨📄✂️

Java Client-Server Application

> [!NOTE]  
> This project is a school assignment.

```
rock-paper-scissors-network/
│── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com.karel.rps/           # Root package
│   │   │   │   ├── client/              # Client-side code
│   │   │   │   │   ├── RPSClient.java   # Main client class
│   │   │   │   │   ├── ClientHandler.java # Handles UI & network communication
│   │   │   │   │   ├── GUI/             # (Optional) GUI-related classes
│   │   │   │   │   └── model/           # Client-side data models
│   │   │   │   ├── server/              # Server-side code
│   │   │   │   │   ├── RPSServer.java   # Main server class
│   │   │   │   │   ├── ServerHandler.java # Manages client connections
│   │   │   │   │   ├── GameSession.java # Handles game logic for each session
│   │   │   │   │   ├── model/           # Server-side game models
│   │   │   │   ├── common/              # Shared utilities & constants
│   │   │   │   │   ├── RPSMove.java     # Enum for Rock, Paper, Scissors
│   │   │   │   │   ├── Protocol.java    # Defines communication protocol
│   │   │   │   │   ├── LoggerUtil.java  # Logging utility
│   ├── test/
│   │   ├── java/
│   │   │   ├── com.karel.rps/
│   │   │   │   ├── client/              # Unit tests for client
│   │   │   │   ├── server/              # Unit tests for server
│   │   │   │   ├── common/              # Tests for shared utilities
│── .gitignore
│── pom.xml
│── README.md
```