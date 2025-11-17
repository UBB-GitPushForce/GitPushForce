import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routes.auth_routes import router as auth_router
from routes.expense_routes import router as expense_router
from routes.group_routes import router as group_router

# Create FastAPI application instance
app = FastAPI(title="GitPushForce API")

# Configure CORS middleware to allow frontend access
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",   # React app
        "http://localhost:5173",   # Vite app
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

# Register routers for expenses, authentication, and groups
app.include_router(expense_router, prefix="/expenses", tags=["Expenses"])
app.include_router(auth_router, prefix="/users", tags=["Auth"])
app.include_router(group_router, prefix="/groups", tags=["Groups"])

# Basic API root endpoint
@app.get("/")
def root():
    return {"message": "API is running"}

# Run Uvicorn server when executed directly
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
