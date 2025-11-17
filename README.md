# 🚀 S-Emulator Program - Modular Client-Server Emulator

> **A Comprehensive Modular Emulator: A Client-Server system for running, expanding, and debugging S-Language code.**

[![GitHub: Victoriamu13/S-emulator](https://img.shields.io/badge/GitHub%20Repository-Victoriamu13%2FS--emulator-blue?style=for-the-badge&logo=github)](https://github.com/Victoriamu13/S-emulator)
[![Technologies](https://img.shields.io/badge/Tech-Java%2C%20Tomcat%2C%20JavaFX%2C%20Servlets-red?style=for-the-badge)](https://www.java.com/)

---

## ✨ System Overview

[cite_start]The **S-Emulator Program** [cite: 3] [cite_start]is a modular client-server system [cite: 7] [cite_start]designed to emulate and execute programs written in a simplified assembly-like language called S-Language[cite: 7].

[cite_start]It combines a **Tomcat-based backend server** (implemented with Servlets) [cite: 8] [cite_start]and a **JavaFX graphical client** [cite: 8] [cite_start]that communicates with it over HTTP[cite: 8, 116]. [cite_start]This architecture allows users to load, expand, and execute S-Language programs, debug them interactively, and manage execution history [cite: 9] [cite_start]within a multi-user, connected environment[cite: 16].

### 💻 Main Capabilities

* [cite_start]**Program Management:** Load and validate S-Language programs from XML definitions[cite: 11].
* [cite_start]**Expansion Engine:** Expand synthetic (high-level) instructions into equivalent base instructions according to a selected degree of expansion[cite: 12].
* [cite_start]**Execution Engine:** Execute programs with user-provided inputs, calculate the final output (`y`), and track the number of execution cycles[cite: 13].
* [cite_start]**Run History:** Maintain a persistent history of previous runs per user, including inputs, outputs, architectures, and cycle counts[cite: 14, 59].
* **Credits System:** Each user has limited execution credits. [cite_start]The server checks and deducts credits before each run[cite: 15, 82].
* [cite_start]**Multi-User Environment:** Each connected client maintains its own session, engine instance, and program state on the server[cite: 16, 80].

---

## 🛠️ Architecture and Technologies

The system is split into three main modules: **Engine (Core Logic), UI (Client), and WEB (Server API)**.

| Module | Primary Role | Key Technologies |
| :--- | :--- | :--- |
| **Engine** | [cite_start]Core Logic: Handles loading, validation, expansion, and execution of S-Language programs[cite: 21]. | Java (Core Logic) |
| **UI** | [cite_start]Client Side: Full JavaFX Graphical User Interface (GUI) and Debugger[cite: 19, 91]. [cite_start]Includes Dashboard and Execution Screen[cite: 92]. | JavaFX, OkHttp, Gson |
| **WEB** | [cite_start]API Server: Manages sessions, and exposes specific Servlets for Client-Server communication (Login, Load, Run, Debug)[cite: 115]. | Apache Tomcat, Servlets |

### 🧠 Engine Module: The Computational Core

[cite_start]The Engine module is completely independent from the UI and server layers[cite: 22].

* [cite_start]**Engine Facade:** Acts as the facade between the UI/servlets and the internal engine logic[cite: 24].
* [cite_start]**Expansion & Execution:** Classes like `ProgramExpander` [cite: 44] [cite_start]and `ProgramExecuter` [cite: 50] [cite_start]manage the process of expansion and sequential instruction execution[cite: 50].
* [cite_start]**Debugging Engine:** The `DebugSession` [cite: 71] [cite_start]controller supports `step()`, `stepOver()`, and `resume()` operations[cite: 72].
* [cite_start]**Function Composition:** Supports synthetic function instructions [cite: 65] [cite_start]and manages function definition validation and argument parsing[cite: 75, 77].

### 🖥️ UI Module: The Graphical Interface

[cite_start]The UI module communicates with the backend via REST endpoints (using OkHttp + Gson) [cite: 91] and provides a comprehensive interface:

* [cite_start]**Dashboard:** Controllers manage user login (`LoginController`) [cite: 97][cite_start], display active users (`UsersTableController`) [cite: 98][cite_start], list programs (`ProgramsTableController`) [cite: 100][cite_start], and show the global function repository (`FunctionsTableController`)[cite: 102].
* [cite_start]**Execution Screen:** Controllers handle live credit balance (`ExecHeaderController`) [cite: 107][cite_start], provide execution controls (`ExecActionsController`) [cite: 108][cite_start], display instructions (`InstructionsTableController`) [cite: 112][cite_start], and manage the expansion chain (`HistoryChainController`)[cite: 113].

### 🌐 WEB Module: The Server API

[cite_start]The WEB module exposes Servlets [cite: 116] [cite_start]that are stateless and communicate using HTTP + JSON[cite: 116, 117].

* [cite_start]**Login & Session Servlets:** Handles user registration (`/login`) and session cleanup (`/logout`)[cite: 120, 121].
* [cite_start]**Program Servlets:** Manages XML file upload and validation (`/loadProgram`)[cite: 123].
* [cite_start]**Execution Servlets:** Executes the selected program in normal mode (`/runProgram`)[cite: 126].
* [cite_start]**Re-Run & History Servlets:** Allows cross-user debugging and run replay [cite: 132][cite_start], and enables re-run mode (`/activateReRun`)[cite: 129].

---

## 🔑 Contact

For any questions, suggestions, or comments about the project, feel free to reach out:

* **Name:** Victoria Musayko
* [cite_start]**Email:** Victoriamus130@gmail.com [cite: 1]
* [cite_start]**GitHub:** [Victoriamu13/S-emulator](https://github.com/Victoriamu13/S-emulator) [cite: 1]

---

## 🤝 Contributing

The project is open for contribution! We welcome:

* Bug reports (Issues)
* Feature requests
* Pull Requests with improvements to the existing code

**Thank you!**
