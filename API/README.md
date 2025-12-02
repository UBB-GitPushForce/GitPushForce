# API

## Setup

1. **Change directory**
```
> cd GitPushForce/API
```

2. **Create a virtual environment**

``` 
> python -m venv .venv
```

3. **Activate the virtual environment**<br>
- Windows (PowerShell):

```
> .venv\Scripts\Activate.ps1
```

- Windows (cmd):

```
> .venv\Scripts\activate
```

- Linux / macOS:

```
> source .venv/bin/activate
```

4. **Upgrade pip and install dependencies**

```
> pip install --upgrade pip
> pip install -r requirements.txt
```

5. **Go to database.py and follow the instructions in the comments**

6. **Check example.py to see how to use the repositories**

----------------------------------------------------------------------------------------

# Backend Architecture Overview

This backend uses FastAPI and follows a clean layered structure.  
Each layer has one clear job, and they communicate in a simple, predictable path:

🧑‍💻 Client → 🔀 Routes → ⚙️ Services → 🗄️ Repositories → 🏛️ Database

---

## 🔀 Routes (API Endpoints)
- Receive and interpret HTTP requests from the client.  
- Extract data, read JWT tokens, and forward everything to the correct service.  
- Contain **no business logic** — only request handling.  
- Act as the “entry gate” of the system.

---

## ⚙️ Services (Business Logic)
- Contain all the rules of the application.  
- Validate inputs, check permissions, and decide what should happen.  
- Communicate directly with repositories to read and write data.  
- Ensure the system behaves correctly and consistently.

---

## 🗄️ Repositories (Database Access)
- Directly interact with the database using SQLAlchemy sessions.  
- Perform CRUD operations: create, read, update, delete.  
- Contain **no logic**, only pure data operations.  
- Act as the link between services and the database.

---

## 🧱 Models and Schemas
**Models**
- Define how data is stored in the database (SQLAlchemy).  
- Represent actual tables and relationships.

**Schemas**
- Define how data enters and leaves the API (Pydantic).  
- Validate request data and format responses.

---

## 🛠️ Helpers
- Provide shared utilities used across the project.  
- Handle tasks like:  
  - 🔐 JWT token creation and decoding  
  - 🔑 Password hashing  
  - 📝 Logging  
  - 📆 Date parsing  
  - 🔢 Generating invitation codes  
- Keep the main logic clean by handling supporting functions.

---

## 🏛️ Database Setup
- Configures the database engine and session.  
- Provides a session instance used by all repositories.  
- Ensures safe connection handling and transaction control.

---

## 🚀 Main Application
- Loads all route modules (users, groups, expenses, etc.).  
- Enables CORS for frontend communication.  
- Starts the FastAPI application and exposes the API.

---

## 🔄 Communication Flow
This is how every request moves through the system:

1. 🧑‍💻 **Client sends a request** (ex: create expense)  
2. 🔀 **Route receives it**, reads token, extracts parameters  
3. ⚙️ **Service validates data**, checks permissions, applies rules  
4. 🗄️ **Repository queries the database** or writes changes  
5. ⚙️ **Service formats the result**  
6. 🔀 **Route returns it** as a response back to the client

The layers never skip each other.  
Each one handles its own job and passes work to the next.
