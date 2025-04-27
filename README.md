![banner](./media/banner.svg)

# Rock-Paper-Scissors

This project is a **multithreaded server-client** application built in Java for the **Rock-Paper-Scissors game**. The server is responsible for accepting multiple client connections, handling communication between them, and managing the game state. The client can interact with the server to play the game.


> [!NOTE]  
> This project is a school assignment.

## 🎮 Game Rules

### 📜 [Official Rules](https://wrpsa.com/the-official-rules-of-rock-paper-scissors/)

- 🪨 **Rock** crushes ✂️ **Scissors**
- ✂️ **Scissors** cuts 📄 **Paper**
- 📄 **Paper** covers 🪨 **Rock**

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [References](#references)
    - [Related Projects](#related-projects)
    - [Tutorials](#tutorials)
- [License](#license)

## Project Overview

The **Rock-Paper-Scissors** game server provides a platform where multiple users can play the game against each other. It features:
- Client-server communication over **TCP sockets**.
- Multithreaded handling of multiple users.
- Logging of server and client activities.
- Proper management of client connections.

## Features

- **Multithreaded server**: Each client is handled in a separate thread, allowing multiple concurrent connections.
- **Game logic**: The server contains the rules for the game and determines the winner.
- **Logging**: Server and client activity are logged for debugging and monitoring purposes.
- **Client Interaction**: Clients can send messages to the server and receive responses to play the game.
- **Connection Management**: The server monitors and handles new client connections.

## Technologies Used

- **Java**: The core language for server and client implementation.
- **TCP/IP**: For communication between server and client.
- **Multithreading**: To handle multiple clients simultaneously.
- **Logging**: For monitoring and debugging server activity.

## References

### Related Projects

Here are some projects that were helpful in creating this project:

- [Multithreaded Server-Client Java](https://github.com/grupp3/Multithreaded-Server-Client-Java-)  
  A basic multithreaded server-client structure that helped with the server-client communication setup.

- [Server Messaging](https://github.com/jelinekp/server)  
  Provided insights into messaging protocols for client-server interactions.

- [Chess Game Server](https://github.com/acadiuss/JAVA)  
  A server-side game logic for managing a chess game, which inspired some of the structure for game state management.

- [Tic-Tac-Toe Server](https://github.com/LukasHaringer/Piskvorky-server-C-klient-JAVA)  
  Helped with managing game logic and networking protocols for client-server communication.

### Tutorials

The following tutorials and resources helped me understand the key concepts needed to implement the server-client communication:

- [How to Create a Simple TCP Client-Server Connection in Java - GeeksforGeeks](https://www.geeksforgeeks.org/how-to-create-a-simple-tcp-client-server-connection-in-java/)

- [Establishing Two-Way Communication Between Server and Client in Java - GeeksforGeeks](https://www.geeksforgeeks.org/establishing-the-two-way-communication-between-server-and-client-in-java/)

- [Java Socket Server Examples (TCP/IP) - CodeJava](https://www.codejava.net/java-se/networking/java-socket-server-examples-tcp-ip)

- [A Guide to Java Sockets - Baeldung](https://www.baeldung.com/a-guide-to-java-sockets)

- [DigitalOcean Tutorial on Java Socket Programming - DigitalOcean](https://www.digitalocean.com/community/tutorials/java-socket-programming-server-client)

- [TCP Connection Between Two Servers in Java - Medium](https://medium.com/@gaurangjotwani/creating-a-tcp-connection-between-two-servers-in-java-27fabe53deaa)

### License

This project is licensed under the MIT License - see the [LICENSE](/LICENSE) file for details.
