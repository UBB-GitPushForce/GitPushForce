import uvicorn
from fastapi import FastAPI
from routes.expense_routes import router as expense_router

app = FastAPI(title="GitPushForce API")

app.include_router(expense_router, prefix="/expenses", tags=["Expenses"])

@app.get("/")
def root():
    return {"message": "API is running"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
