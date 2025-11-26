import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routes.auth_routes import router as auth_router
from routes.expense_payment_routes import router as expense_payment_router
from routes.expense_routes import router as expense_router
from routes.group_log_routes import router as group_log_router
from routes.group_routes import router as group_router
from routes.user_routes import router as user_router

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
app.include_router(user_router, prefix="/users", tags=["Users"])
app.include_router(expense_payment_router, prefix="/expenses_payments")
app.include_router(group_log_router, prefix="/group_logs")

# Basic API root endpoint
@app.get("/")
def root():
    return {"message": "API is running"}

# Run Uvicorn server when executed directly
if __name__ == "__main__":
    try:
        uvicorn.run(app, host="0.0.0.0", port=8000)
    except KeyboardInterrupt:
        print("Goodbye.")
