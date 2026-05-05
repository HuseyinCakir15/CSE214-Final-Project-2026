import mysql.connector
import os
from dotenv import load_dotenv
from pathlib import Path
from mysql.connector.pooling import MySQLConnectionPool

load_dotenv(dotenv_path=Path(__file__).parent / ".env")

# Her sorguda yeni bağlantı yerine connection pool
pool = MySQLConnectionPool(
    pool_name="chatbot_pool",
    pool_size=5,
    host=os.getenv("DB_HOST", "localhost"),
    port=int(os.getenv("DB_PORT", 3306)),
    database=os.getenv("DB_NAME", "ecommerce_db"),
    user=os.getenv("DB_USER", "root"),
    password=os.getenv("DB_PASSWORD", "")
)

def get_connection():
    return pool.get_connection()

# SQL injection koruması
FORBIDDEN_KEYWORDS = [
    "DROP", "DELETE", "INSERT", "UPDATE", "TRUNCATE",
    "ALTER", "CREATE", "REPLACE", "EXEC", "EXECUTE",
    "GRANT", "REVOKE", "COMMIT", "ROLLBACK"
]

def execute_query(sql: str) -> list:
    sql_stripped = sql.strip()

    # Sadece SELECT'e izin ver
    if not sql_stripped.upper().startswith("SELECT"):
        raise ValueError("Sadece SELECT sorguları çalıştırılabilir!")

    # Tehlikeli anahtar kelime kontrolü
    sql_upper = sql_stripped.upper()
    for kw in FORBIDDEN_KEYWORDS:
        if kw in sql_upper:
            raise ValueError(f"Tehlikeli SQL komutu engellendi: {kw}")

    conn = get_connection()
    cursor = conn.cursor(dictionary=True)
    try:
        cursor.execute(sql_stripped)
        results = cursor.fetchall()
        return results
    except Exception as e:
        raise e
    finally:
        cursor.close()
        conn.close()

def get_schema() -> str:
    return """
    DATABASE SCHEMA (ecommerce_db - MySQL):

    TABLE: users
      id            BIGINT PK
      email         VARCHAR(150) UNIQUE
      password_hash VARCHAR(255)
      role_type     ENUM('admin', 'corporate', 'individual')
      gender        ENUM('male', 'female', 'other', 'unspecified')
      created_at    DATETIME
      updated_at    DATETIME

    TABLE: customer_profiles  (linked to individual users)
      id               BIGINT PK
      user_id          BIGINT FK → users.id (UNIQUE)
      age              INT
      city             VARCHAR(100)
      membership_type  ENUM('Bronze', 'Silver', 'Gold')
      total_spend      DECIMAL(12,2)
      satisfaction_level VARCHAR(50)
      created_at       DATETIME
      updated_at       DATETIME

    TABLE: stores  (owned by corporate users)
      id         BIGINT PK
      owner_id   BIGINT FK → users.id
      name       VARCHAR(150)
      status     ENUM('open', 'closed')
      created_at DATETIME
      updated_at DATETIME

    TABLE: categories
      id        BIGINT PK
      name      VARCHAR(100)
      parent_id BIGINT FK → categories.id (nullable, for subcategories)

    TABLE: products
      id               BIGINT PK
      store_id         BIGINT FK → stores.id
      category_id      BIGINT FK → categories.id (nullable)
      sku              VARCHAR(100)
      name             VARCHAR(500)
      unit_price       DECIMAL(10,2)
      discounted_price DECIMAL(10,2) (nullable)
      stock            INT
      rating           DECIMAL(3,1)
      created_at       DATETIME
      updated_at       DATETIME

    TABLE: orders
      id             BIGINT PK
      user_id        BIGINT FK → users.id
      store_id       BIGINT FK → stores.id
      status         ENUM('pending','confirmed','shipped','delivered','cancelled','returned','rejected','refunded')
      grand_total    DECIMAL(10,2)
      payment_method VARCHAR(50)
      created_at     DATETIME   ← sipariş tarihi için bu kolonu kullan
      updated_at     DATETIME

    TABLE: order_items
      id         BIGINT PK
      order_id   BIGINT FK → orders.id
      product_id BIGINT FK → products.id
      quantity   INT
      price      DECIMAL(10,2)
      created_at DATETIME

    TABLE: shipments
      id         BIGINT PK
      order_id   BIGINT FK → orders.id (UNIQUE, 1 order = 1 shipment)
      warehouse  VARCHAR(100)
      mode       ENUM('Road', 'Air', 'Ship')
      status     ENUM('pending', 'in_transit', 'delivered', 'returned')
      city       VARCHAR(100)
      state      VARCHAR(100)
      created_at DATETIME
      updated_at DATETIME

    TABLE: reviews
      id           BIGINT PK
      user_id      BIGINT FK → users.id
      product_id   BIGINT FK → products.id
      star_rating  DECIMAL(3,1)  (0.0 - 5.0)
      sentiment    ENUM('positive', 'neutral', 'negative')
      review_title VARCHAR(500)
      review_text  TEXT
      created_at   DATETIME
      updated_at   DATETIME

    TABLE: comments
      id           BIGINT PK
      user_id      BIGINT FK → users.id
      product_id   BIGINT FK → products.id
      review_id    BIGINT FK → reviews.id (nullable)
      comment_text TEXT
      created_at   DATETIME
      updated_at   DATETIME

    KEY RELATIONSHIPS:
    - users → orders        (one user has many orders)
    - orders → order_items  (one order has many items)
    - order_items → products (each item is a product)
    - orders → shipments    (one order has one shipment)
    - users → reviews       (one user writes many reviews)
    - products → reviews    (one product has many reviews)
    - stores → products     (one store sells many products)
    - stores → orders       (orders are placed to a store)

    COMMON QUERY PATTERNS:
    - "en son sipariş": ORDER BY o.created_at DESC LIMIT 1
    - "son N sipariş": ORDER BY o.created_at DESC LIMIT N
    - "toplam harcama": SUM(o.grand_total)
    - "en çok satan ürün": GROUP BY oi.product_id ORDER BY SUM(oi.quantity) DESC
    - "ortalama puan": AVG(r.star_rating)
    - "bu ayki": WHERE MONTH(o.created_at) = MONTH(CURDATE()) AND YEAR(o.created_at) = YEAR(CURDATE())
    """