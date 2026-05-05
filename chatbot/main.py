from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
from agents import ask

app = FastAPI(title="E-Commerce Chatbot API")

# Spring Boot (8080) ve Angular (4200) izin listesinde
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
    sql: Optional[str] = None
    data: list = []
    plotly_json: Optional[str] = None

@app.get("/")
def root():
    return {"status": "Chatbot API is running!"}

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/chat/ask", response_model=ChatResponse)
def chat(request: ChatRequest):
    result = ask(request.question, request.role, request.user_id)
    return ChatResponse(
        answer=result["answer"],
        sql=result.get("sql"),
        data=result.get("data", []),
        plotly_json=result.get("plotly_json")
    )