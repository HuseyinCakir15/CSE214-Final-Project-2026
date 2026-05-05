import os
import time
from google import genai
from dotenv import load_dotenv
from pathlib import Path
from typing import TypedDict, Optional
from langgraph.graph import StateGraph, END
from database import execute_query, get_schema

load_dotenv(dotenv_path=Path(__file__).parent / ".env")
client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))
MODEL = "gemini-2.5-flash"

# ─── MOCK CEVAPLAR (API çökünce veya hata olunca) ─────────────────────────────
MOCK_DB = {
    "en son": "En son siparişlerinizi 'Siparişlerim' sekmesinden görebilirsiniz.",
    "son sipariş": "En son siparişlerinizi 'Siparişlerim' sekmesinden görebilirsiniz.",
    "toplam": "Toplam harcama ve sipariş bilgileriniz dashboard'unuzda görüntülenmektedir.",
    "gelir": "Mağaza geliriniz dashboard'unuzda anlık olarak takip edilmektedir.",
    "en çok": "En çok satan ürünlerinizi ürünler sekmesinden inceleyebilirsiniz.",
    "kaç": "Sipariş sayınızı dashboard'unuzdan görebilirsiniz.",
    "merhaba": "Merhaba! Size nasıl yardımcı olabilirim? Siparişler, ürünler veya satışlar hakkında sorabilirsiniz.",
    "hello": "Hello! How can I help you? You can ask about orders, products, or sales.",
}

def get_mock_response(question: str) -> str:
    q = question.lower()
    for key, response in MOCK_DB.items():
        if key in q:
            return response
    return "Şu an AI asistan geçici olarak yanıt veremiyor. Lütfen birkaç dakika sonra tekrar deneyin."

# ─── GEMİNİ API ÇAĞRISI ────────────────────────────────────────────────────────
def generate(prompt: str, retries: int = 2) -> str:
    for attempt in range(retries):
        try:
            response = client.models.generate_content(model=MODEL, contents=prompt)
            return response.text or ""
        except Exception as e:
            err = str(e)
            print(f"API attempt {attempt+1} error: {err[:100]}")
            if "503" in err or "UNAVAILABLE" in err:
                if attempt < retries - 1:
                    time.sleep(2)  # 2 saniye bekle, tekrar dene
                continue
            if "429" in err or "RESOURCE_EXHAUSTED" in err:
                return ""  # Kota doldu, mock'a düş
            return ""  # Diğer hatalar
    return ""

# ─── STATE ─────────────────────────────────────────────────────────────────────
class AgentState(TypedDict):
    question: str
    role: str
    user_id: int
    sql_query: Optional[str]
    query_result: Optional[list]
    error: Optional[str]
    final_answer: Optional[str]
    plotly_json: Optional[str]
    is_in_scope: Optional[bool]
    iteration_count: int
    use_mock: bool  # True olursa mock cevap döner

# ─── ROL CONTEXT ───────────────────────────────────────────────────────────────
def get_role_context(role: str, user_id: int) -> str:
    if role == "individual":
        return f"""
        INDIVIDUAL user (user_id={user_id}).
        - Orders: WHERE o.user_id = {user_id}
        - Reviews: WHERE r.user_id = {user_id}
        - Products: public, no filter
        FORBIDDEN: other users, store revenues, platform analytics
        """
    elif role == "corporate":
        return f"""
        CORPORATE store owner (user_id={user_id}).
        - Store: JOIN stores s ON s.owner_id = {user_id}
        - Products: JOIN products p ON p.store_id = s.id
        - Orders: JOIN orders o ON o.store_id = s.id
        FORBIDDEN: other stores, other users personal info
        """
    else:
        return "ADMIN: Full access to all data."

# ─── AGENTS ────────────────────────────────────────────────────────────────────
def guardrails_agent(state: AgentState) -> AgentState:
    prompt = f"""Is this e-commerce related? Reply ONLY "yes" or "no".
    Question: "{state["question"]}"
    IN SCOPE: orders, products, sales, revenue, shipments, reviews, stores, greetings
    OUT OF SCOPE: weather, sports, politics, cooking, general knowledge"""

    answer = generate(prompt)
    if not answer:
        state["use_mock"] = True
        state["is_in_scope"] = False
    else:
        state["is_in_scope"] = "no" not in answer.strip().lower()
        state["use_mock"] = False
    return state

def get_fallback_sql(question: str, role: str, user_id: int):
    """Gemini başarısız olursa devreye giren hazır SQL şablonları."""
    q = question.lower()
    uid = user_id

    if role == "individual":
        if any(k in q for k in ["en son", "son sipariş", "ne aldım", "ne sipariş ettim", "last order", "son alışveriş", "ne satın"]):
            return f"""
                SELECT o.id, o.status, o.grand_total, o.payment_method, o.created_at,
                       p.name as product_name, oi.quantity, oi.price
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                JOIN products p ON p.id = oi.product_id
                WHERE o.user_id = {uid}
                ORDER BY o.created_at DESC
                LIMIT 10
            """
        if any(k in q for k in ["toplam harcama", "ne kadar harcadım", "harcamalarım"]):
            return f"SELECT SUM(grand_total) as toplam_harcama FROM orders WHERE user_id = {uid}"
        if any(k in q for k in ["kaç sipariş", "sipariş sayı", "kaç tane"]):
            return f"SELECT COUNT(*) as siparis_sayisi FROM orders WHERE user_id = {uid}"
        if any(k in q for k in ["siparişlerim", "tüm siparişler", "my orders"]):
            return f"""
                SELECT o.id, o.status, o.grand_total, o.created_at
                FROM orders o WHERE o.user_id = {uid}
                ORDER BY o.created_at DESC LIMIT 20
            """
    elif role == "corporate":
        if any(k in q for k in ["en çok satan", "çok satan", "top ürün", "best selling"]):
            return f"""
                SELECT p.name, SUM(oi.quantity) as toplam_satis
                FROM order_items oi
                JOIN products p ON p.id = oi.product_id
                JOIN stores s ON s.id = p.store_id
                WHERE s.owner_id = {uid}
                GROUP BY p.id, p.name
                ORDER BY toplam_satis DESC LIMIT 10
            """
        if any(k in q for k in ["toplam gelir", "revenue", "kazanç", "gelir"]):
            return f"""
                SELECT SUM(o.grand_total) as toplam_gelir
                FROM orders o JOIN stores s ON s.id = o.store_id
                WHERE s.owner_id = {uid}
            """
        if any(k in q for k in ["kaç sipariş", "sipariş sayı", "kaç tane"]):
            return f"""
                SELECT COUNT(*) as siparis_sayisi
                FROM orders o JOIN stores s ON s.id = o.store_id
                WHERE s.owner_id = {uid}
            """
    return None

def sql_agent(state: AgentState) -> AgentState:
    if state.get("use_mock"):
        return state

    # 1) Önce fallback SQL dene — Gemini'ye gerek yok, anında çalışır
    fallback = get_fallback_sql(state["question"], state["role"], state["user_id"])
    if fallback:
        state["sql_query"] = fallback.strip()
        state["error"] = None
        return state

    # 2) Fallback yoksa Gemini'den SQL üret
    prompt = f"""You are an expert MySQL query generator.
{get_schema()}
ROLE: {get_role_context(state["role"], state["user_id"])}
Question: "{state["question"]}"

RULES:
1. Return ONLY raw SQL - no markdown, no backticks, no explanation
2. Only SELECT statements allowed
3. Max LIMIT 50
4. Use aliases: orders o, products p, order_items oi, stores s
5. "en son/last" -> ORDER BY o.created_at DESC
6. Exact column names from schema only"""

    sql = generate(prompt)
    if not sql:
        state["use_mock"] = True
        return state

    sql = sql.replace("```sql", "").replace("```mysql", "").replace("```", "").strip()
    if not sql.upper().lstrip().startswith("SELECT"):
        state["use_mock"] = True
        return state

    state["sql_query"] = sql
    state["error"] = None
    return state

def execute_sql(state: AgentState) -> AgentState:
    if state.get("use_mock"):
        return state
    try:
        state["query_result"] = execute_query(state["sql_query"])
        state["error"] = None
    except Exception as e:
        state["error"] = str(e)
        state["query_result"] = None
    return state

def error_agent(state: AgentState) -> AgentState:
    if state.get("use_mock"):
        return state

    prompt = f"""Fix this SQL. Return ONLY corrected SQL, no markdown.
Schema: {get_schema()}
Question: {state["question"]}
Failed SQL: {state["sql_query"]}
Error: {state["error"]}"""

    sql = generate(prompt)
    if not sql:
        state["use_mock"] = True
        return state

    sql = sql.replace("```sql", "").replace("```mysql", "").replace("```", "").strip()
    state["sql_query"] = sql
    state["iteration_count"] = state.get("iteration_count", 0) + 1
    return state

def analysis_agent(state: AgentState) -> AgentState:
    if state.get("use_mock"):
        state["final_answer"] = get_mock_response(state["question"])
        return state

    results = state.get("query_result")
    if not results:
        state["final_answer"] = "Bu sorgu için sonuç bulunamadı."
        return state

    prompt = f"""Data analyst. Answer user question based on results.
Question: "{state["question"]}"
Results ({len(results)} rows): {results[:15]}
- Same language as question (Turkish/English)
- Specific with actual numbers/names
- Max 150 words
- Nice number format (1,234.56 TL)"""

    answer = generate(prompt)
    state["final_answer"] = answer.strip() if answer else get_mock_response(state["question"])
    return state

def visualization_agent(state: AgentState) -> AgentState:
    if state.get("use_mock"):
        state["plotly_json"] = None
        return state

    import pandas as pd
    import plotly.express as px
    import json

    results = state.get("query_result")
    if not results or len(results) < 2:
        state["plotly_json"] = None
        return state

    prompt = f"""Choose best chart. Return ONLY JSON:
{{"chart_type": "bar", "x": "col", "y": "col", "title": "Title"}}
Question: "{state["question"]}"
Columns: {list(results[0].keys())}
Sample: {results[:2]}
Types: bar, line, pie"""

    raw = generate(prompt)
    if not raw:
        state["plotly_json"] = None
        return state

    raw = raw.strip().replace("```json", "").replace("```", "").strip()
    try:
        config = json.loads(raw)
        df = pd.DataFrame(results)
        ct = config.get("chart_type", "bar")
        x, y = config.get("x"), config.get("y")
        title = config.get("title", state["question"])
        if ct == "bar":    fig = px.bar(df, x=x, y=y, title=title)
        elif ct == "line": fig = px.line(df, x=x, y=y, title=title)
        elif ct == "pie":  fig = px.pie(df, names=x, values=y, title=title)
        else:              fig = px.bar(df, x=x, y=y, title=title)
        state["plotly_json"] = fig.to_json()
    except Exception as e:
        print(f"Viz error: {e}")
        state["plotly_json"] = None
    return state

def out_of_scope(state: AgentState) -> AgentState:
    if state.get("use_mock"):
        state["final_answer"] = get_mock_response(state["question"])
    else:
        state["final_answer"] = "Bu soru kapsam dışında. Siparişler, ürünler, satışlar hakkında sorabilirsiniz."
    state["plotly_json"] = None
    return state

# ─── ROUTER ────────────────────────────────────────────────────────────────────
def check_scope(state: AgentState) -> str:
    if state.get("use_mock"):
        return "out_of_scope"
    return "sql_agent" if state["is_in_scope"] else "out_of_scope"

def check_sql(state: AgentState) -> str:
    if state.get("use_mock"):
        return "analysis_agent"
    return "execute_sql"

def check_error(state: AgentState) -> str:
    if state.get("use_mock"):
        return "analysis_agent"
    if state["error"] and state.get("iteration_count", 0) < 2:
        return "error_agent"
    return "analysis_agent"

def check_visualization(state: AgentState) -> str:
    if state.get("use_mock"):
        return END
    results = state.get("query_result")
    return "visualization_agent" if results and len(results) > 1 else END

# ─── GRAPH ─────────────────────────────────────────────────────────────────────
def create_graph():
    graph = StateGraph(AgentState)
    graph.add_node("guardrails_agent",    guardrails_agent)
    graph.add_node("sql_agent",           sql_agent)
    graph.add_node("execute_sql",         execute_sql)
    graph.add_node("error_agent",         error_agent)
    graph.add_node("analysis_agent",      analysis_agent)
    graph.add_node("visualization_agent", visualization_agent)
    graph.add_node("out_of_scope",        out_of_scope)

    graph.set_entry_point("guardrails_agent")
    graph.add_conditional_edges("guardrails_agent", check_scope, {
        "sql_agent": "sql_agent", "out_of_scope": "out_of_scope"
    })
    graph.add_conditional_edges("sql_agent", check_sql, {
        "execute_sql": "execute_sql", "analysis_agent": "analysis_agent"
    })
    graph.add_conditional_edges("execute_sql", check_error, {
        "error_agent": "error_agent", "analysis_agent": "analysis_agent"
    })
    graph.add_edge("error_agent", "execute_sql")
    graph.add_conditional_edges("analysis_agent", check_visualization, {
        "visualization_agent": "visualization_agent", END: END
    })
    graph.add_edge("visualization_agent", END)
    graph.add_edge("out_of_scope", END)
    return graph.compile()

chatbot = create_graph()

# ─── ENTRY POINT ───────────────────────────────────────────────────────────────
def ask(question: str, role: str, user_id: int) -> dict:
    state: AgentState = {
        "question": question, "role": role, "user_id": user_id,
        "sql_query": None, "query_result": None, "error": None,
        "final_answer": None, "plotly_json": None,
        "is_in_scope": None, "iteration_count": 0, "use_mock": False
    }
    try:
        result = chatbot.invoke(state)
        return {
            "answer":      result.get("final_answer") or get_mock_response(question),
            "sql":         result.get("sql_query"),
            "data":        result.get("query_result") or [],
            "plotly_json": result.get("plotly_json")
        }
    except Exception as e:
        import traceback
        print("CHATBOT ERROR:", traceback.format_exc())
        return {
            "answer":      get_mock_response(question),
            "sql":         None,
            "data":        [],
            "plotly_json": None
        }