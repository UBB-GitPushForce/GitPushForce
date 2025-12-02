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

------------------------------------------------------------------------
# Backend Architecture Overview

This backend uses FastAPI with a simple layered structure.  
Each layer has one job, and the system communicates in a straight flow:

Client → Routes → Services → Repositories → Database

## Routes
Handle incoming requests.  
Extract data and user identity from JWT.  
Call the correct service.  
No logic inside.

## Services
Hold the application rules.  
Validate input, check permissions, and decide what should happen.  
Use repositories for all database operations.

## Repositories
Talk to the database with SQLAlchemy.  
Perform create, read, update, and delete actions.  
Contain no business rules.

## Models and Schemas
Models define how data is stored.  
Schemas define how data enters and leaves the API.

## Helpers
Provide utilities for JWT, password hashing, logging, date parsing, and code generation.  
Support the system without holding business logic.

## Database Setup
Creates the database engine and session.  
Repositories use this session to communicate with the database.

## Main Application
Loads all routes.  
Configures CORS and starts the FastAPI application.

## Communication Flow
1. Client sends a request  
2. Route receives it  
3. Service processes it  
4. Repository reads or writes data  
5. Service prepares the response  
6. Route returns it to the client

This structure keeps the backend simple, clear, and easy to maintain.
