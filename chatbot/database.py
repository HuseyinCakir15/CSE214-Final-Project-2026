import mysql.connector
import os
from dotenv import load_dotenv
from pathlib import Path

load_dotenv(dotenv_path=Path(__file__).parent / ".env")

def get_connection():
    return mysql.connector.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", 3306)),
        database=os.getenv("DB_NAME", "ecommerce_db"),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PASSWORD", "")
    )

def execute_query(sql: str) -> list:
    conn = get_connection()
    cursor = conn.cursor(dictionary=True)
    try:
        cursor.execute(sql)
        results = cursor.fetchall()
        return results
    except Exception as e:
        raise e
    finally:
        cursor.close()
        conn.close()

def get_schema() -> str:
    return """
    DATABASE SCHEMA (ecommerce_db):

    users (id, email, password_hash, role_type, gender)
    stores (id, name, status, owner_id, created_at)
    categories (id, name, parent_id)
    products (id, store_id, category_id, sku, name, unit_price, discounted_price, stock, rating)
    orders (id, user_id, store_id, status, grand_total, payment_method, created_at)
    order_items (id, order_id, product_id, quantity, price)
    shipments (id, order_id, warehouse, mode, status, city, state, created_at)
    reviews (id, user_id, product_id, star_rating, sentiment, review_title, review_text)
    """