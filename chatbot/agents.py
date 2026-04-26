import os
import google.generativeai as genai
from dotenv import load_dotenv
from pathlib import Path
from typing import TypedDict, Optional, Any
from langgraph.graph import StateGraph, END
from database import execute_query, get_schema

load_dotenv(dotenv_path=Path(__file__).parent / ".env")
genai.configure(api_key=os.getenv("GEMINI_API_KEY"))
model = genai.GenerativeModel("gemini-2.0-flash-lite")

class AgentState(TypedDict):
    question: str
    role: str
    user_id: int
    sql_query: Optional[str]
    query_result: Optional[list]
    error: Optional[str]
    final_answer: Optional[str]
    is_in_scope: Optional[bool]
    iteration_count: int

def get_role_context(role: str, user_id: int) -> str:
    if role == "individual":
        return f"""
        You are serving an individual user (user_id={user_id}).
        ALLOWED: General product info (names, prices, ratings, categories).
        ALLOWED: Their own orders - always add WHERE o.user_id={user_id}
        ALLOWED: Their own reviews - always add WHERE r.user_id={user_id}
        FORBIDDEN: Other users data, store revenues, platform analytics.
        """
    elif role == "corporate":
        return f"""
        You are serving a corporate user (user_id={user_id}).
        ALLOWED: Their own store data - always JOIN stores WHERE owner_id={user_id}
        ALLOWED: Products, orders, reviews belonging to their store.
        ALLOWED: General category info.
        FORBIDDEN: Other stores data, other users personal info.
        """
    else:
        return "You have full access to all platform data. No restrictions."

def guardrails_agent(state: AgentState) -> AgentState:
    question = state["question"]
    prompt = f"""
    You are a guardrails system for an e-commerce analytics chatbot.
    Is this question relevant to e-commerce data analysis?
    Question: "{question}"
    Reply with only "yes" or "no".
    - "yes": orders, products, users, sales, revenue, shipments, reviews, stores, categories, greetings
    - "no": weather, sports, politics, general knowledge unrelated to ecommerce
    """
    response = model.generate_content(prompt)
    answer = response.text.strip().lower()
    state["is_in_scope"] = "yes" in answer
    return state

def sql_agent(state: AgentState) -> AgentState:
    schema = get_schema()
    role_context = get_role_context(state["role"], state["user_id"])
    prompt = f"""
    You are a SQL expert. Generate a valid MySQL SELECT query.
    
    {schema}
    
    ROLE RESTRICTIONS (STRICTLY FOLLOW):
    {role_context}
    
    Question: {state["question"]}
    
    Rules:
    - Return ONLY the SQL query, no explanation, no markdown, no backticks
    - Only SELECT statements, never INSERT/UPDATE/DELETE/DROP
    - Limit results to 100 rows maximum
    - Use proper JOINs when needed
    """
    response = model.generate_content(prompt)
    sql = response.text.strip()
    sql = sql.replace("```sql", "").replace("```", "").strip()
    state["sql_query"] = sql
    state["error"] = None
    return state

def execute_sql(state: AgentState) -> AgentState:
    try:
        results = execute_query(state["sql_query"])
        state["query_result"] = results
        state["error"] = None
    except Exception as e:
        state["error"] = str(e)
        state["query_result"] = None
    return state

def error_agent(state: AgentState) -> AgentState:
    schema = get_schema()
    prompt = f"""
    You are a SQL error recovery specialist.
    {schema}
    Original question: {state["question"]}
    Failed SQL: {state["sql_query"]}
    Error: {state["error"]}
    Fix the SQL query. Return ONLY the corrected SQL, no explanation.
    """
    response = model.generate_content(prompt)
    sql = response.text.strip()
    sql = sql.replace("```sql", "").replace("```", "").strip()
    state["sql_query"] = sql
    state["iteration_count"] = state.get("iteration_count", 0) + 1
    return state

def analysis_agent(state: AgentState) -> AgentState:
    prompt = f"""
    You are a data analyst. Explain these query results in simple, clear language.
    Question: {state["question"]}
    SQL Query: {state["sql_query"]}
    Results: {state["query_result"]}
    Provide a concise, helpful answer in the same language as the question.
    If results are empty, say so clearly. Keep it under 200 words.
    """
    response = model.generate_content(prompt)
    state["final_answer"] = response.text.strip()
    return state

def out_of_scope(state: AgentState) -> AgentState:
    state["final_answer"] = "Bu soru e-ticaret veri analizi kapsamı dışındadır. Lütfen ürünler, siparişler, satışlar veya müşteriler hakkında sorular sorun."
    return state

def check_scope(state: AgentState) -> str:
    return "sql_agent" if state["is_in_scope"] else "out_of_scope"

def check_error(state: AgentState) -> str:
    if state["error"] and state.get("iteration_count", 0) < 3:
        return "error_agent"
    return "analysis_agent"

def create_graph():
    graph = StateGraph(AgentState)
    graph.add_node("guardrails_agent", guardrails_agent)
    graph.add_node("sql_agent", sql_agent)
    graph.add_node("execute_sql", execute_sql)
    graph.add_node("error_agent", error_agent)
    graph.add_node("analysis_agent", analysis_agent)
    graph.add_node("out_of_scope", out_of_scope)
    graph.set_entry_point("guardrails_agent")
    graph.add_conditional_edges("guardrails_agent", check_scope, {
        "sql_agent": "sql_agent",
        "out_of_scope": "out_of_scope"
    })
    graph.add_edge("sql_agent", "execute_sql")
    graph.add_conditional_edges("execute_sql", check_error, {
        "error_agent": "error_agent",
        "analysis_agent": "analysis_agent"
    })
    graph.add_edge("error_agent", "execute_sql")
    graph.add_edge("analysis_agent", END)
    graph.add_edge("out_of_scope", END)
    return graph.compile()

chatbot = create_graph()

def ask(question: str, role: str, user_id: int) -> dict:
    state: AgentState = {
        "question": question,
        "role": role,
        "user_id": user_id,
        "sql_query": None,
        "query_result": None,
        "error": None,
        "final_answer": None,
        "is_in_scope": None,
        "iteration_count": 0
    }
    result = chatbot.invoke(state)
    return {
        "answer": result["final_answer"],
        "sql": result.get("sql_query"),
        "data": result.get("query_result", [])
    }