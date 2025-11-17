# 🚀 S-Emulator Program - Modular Client-Server Emulator

> **A Comprehensive Modular Emulator: A Client-Server system for running, expanding, and debugging S-Language code.**

[![GitHub: Victoriamu13/S-emulator](https://img.shields.io/badge/GitHub%20Repository-Victoriamu13%2FS--emulator-blue?style=for-the-badge&logo=github)](https://github.com/Victoriamu13/S-emulator)
[![Technologies](https://img.shields.io/badge/Tech-Java%2C%20Tomcat%2C%20JavaFX%2C%20Servlets-red?style=for-the-badge)](https://www.java.com/)
---

## ✨ System Overview

The **S-Emulator Program** is a modular client-server system designed to emulate and execute programs written in a simplified assembly-like language called S-Language.

It combines a **Tomcat-based backend server** (implemented with Servlets) and a **JavaFX graphical client** that communicates with it over HTTP. This architecture allows users to load, expand, and execute S-Language programs, debug them interactively, and manage execution history within a multi-user, connected environment.

### 💻 Main Capabilities

* **Program Management:** Load and validate S-Language programs from XML definitions.
* **Expansion Engine:** Expand synthetic (high-level) instructions into equivalent base instructions according to a selected degree of expansion.
* **Execution Engine:** Execute programs with user-provided inputs, calculate the final output (`y`), and track the number of execution cycles.
* **Run History:** Maintain a persistent history of previous runs per user, including inputs, outputs, architectures, and cycle counts.
* **Credits System:** Each user has limited execution credits. The server checks and deducts credits before each run.
* **Multi-User Environment:** Each connected client maintains its own session, engine instance, and program state on the server.

---

## 🛠️ Architecture and Technologies

The system is split into three main modules: **Engine (Core Logic), UI (Client), and WEB (Server API)**.

| Module | Primary Role | Key Technologies |
| :--- | :--- | :--- |
| **Engine** | Core Logic: Handles loading, validation, expansion, and execution of S-Language programs. | Java (Core Logic) |
| **UI** | Client Side: Full JavaFX Graphical User Interface (GUI) and Debugger. Includes Dashboard and Execution Screen. | JavaFX, OkHttp, Gson |
| **WEB** | API Server: Manages sessions, and exposes specific Servlets for Client-Server communication (Login, Load, Run, Debug). | Apache Tomcat, Servlets |

### 🧠 Engine Module: Logic Core

The Engine module is completely independent from the UI and server layers.

* **Engine Facade:** Acts as the facade between the UI/servlets and the internal engine logic.
* **Expansion & Execution:** Classes like `ProgramExpander` and `ProgramExecuter` manage the process of expansion and sequential instruction execution.
* **Debugging Engine:** The `DebugSession` controller supports `step()`, `stepOver()`, and `resume()` operations.
* **Function Composition:** Supports synthetic function instructions and manages function definition validation and argument parsing.

### 🖥️ UI Module: The Graphical Interface

The UI module communicates with the backend via REST endpoints (using OkHttp + Gson) and provides a comprehensive interface:

* **Dashboard:** Controllers manage user login (`LoginController`), display active users (`UsersTableController`), list programs (`ProgramsTableController`), and show the global function repository (`FunctionsTableController`).
* **Execution Screen:** Controllers handle live credit balance (`ExecHeaderController`), provide execution controls (`ExecActionsController`), display instructions (`InstructionsTableController`), and manage the expansion chain (`HistoryChainController`).

### 🌐 WEB Module: The Server API

The WEB module exposes Servlets that are stateless and communicate using HTTP + JSON.

* **Login & Session Servlets:** Handles user registration (`/login`) and session cleanup (`/logout`).
* **Program Servlets:** Manages XML file upload and validation (`/loadProgram`).
* **Execution Servlets:** Executes the selected program in normal mode (`/runProgram`).
* **Re-Run & History Servlets:** Allows cross-user debugging and run replay, and enables re-run mode (`/activateReRun`).



