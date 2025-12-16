import uvicorn
from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routes.auth_routes import router as auth_router
from routes.category_routes import router as category_router
from routes.expense_payment_routes import router as expense_payment_router
from routes.expense_routes import router as expense_router
from routes.group_log_routes import router as group_log_router
from routes.group_routes import router as group_router

# ReceiptService specific
from routes.receipt_routes import router as receipt_router
from routes.user_routes import router as user_router

app = FastAPI(title="GitPushForce API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",   # React 
        "http://localhost:5173",   # Vite 
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

app.include_router(expense_router, prefix="/expenses", tags=["Expenses"])
app.include_router(auth_router, prefix="/users", tags=["Auth"])
app.include_router(group_router, prefix="/groups", tags=["Groups"])
app.include_router(user_router, prefix="/users", tags=["Users"])
app.include_router(category_router, prefix="/categories", tags=["Categories"])
app.include_router(expense_payment_router, prefix="/expenses_payments")
app.include_router(group_log_router, prefix="/group_logs")
app.include_router(receipt_router, prefix="/receipt")

@app.get("/")
def root():
    return {"message": "API is running"}

if __name__ == "__main__":
    try:
        load_dotenv()
        uvicorn.run(app, host="0.0.0.0", port=8000)
    except KeyboardInterrupt:
        print("Goodbye.")
