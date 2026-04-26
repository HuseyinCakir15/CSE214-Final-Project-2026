from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from agents import ask

app = FastAPI(title="E-Commerce Chatbot API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:4200", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class ChatRequest(BaseModel):
    question: str
    role: str
    user_id: int

class ChatResponse(BaseModel):
    answer: str
    sql: str | None = None
    data: list = []

@app.get("/")
def root():
    return {"status": "Chatbot API is running!"}

@app.post("/chat/ask", response_model=ChatResponse)
def chat(request: ChatRequest):
    try:
        result = ask(request.question, request.role, request.user_id)
        return ChatResponse(
            answer=result["answer"],
            sql=result.get("sql"),
            data=result.get("data", [])
        )
    except Exception as e:
        from fastapi import HTTPException
        raise HTTPException(status_code=500, detail=str(e))