# db_loader.py
import pymysql
import os
from dotenv import load_dotenv
from datetime import datetime

load_dotenv()

DB_CFG = {
    "host":     os.getenv("DB_HOST", "localhost"),
    "user":     os.getenv("DB_USER", "root"),
    "password": os.getenv("DB_PASS", ""),
    "db":       os.getenv("DB_NAME", "SMS"),
    "charset":  os.getenv("DB_CHARSET", "utf8"),
}

# 📌 이미 있을 함수
def load_dataset(limit=None):
    conn = pymysql.connect(**DB_CFG)
    cursor = conn.cursor()
    query = "SELECT text, label FROM sms_dataset"
    if limit:
        query += f" LIMIT {limit}"
    cursor.execute(query)
    rows = cursor.fetchall()
    conn.close()

    texts = [row[0] for row in rows]
    labels = [row[1] for row in rows]

    from sklearn.feature_extraction.text import TfidfVectorizer
    vectorizer = TfidfVectorizer()
    matrix = vectorizer.fit_transform(texts)

    return texts, labels, vectorizer, matrix

def insert_detection_history(user_id, sender, message, matched, similarity, result):
    conn = pymysql.connect(**DB_CFG)
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO detection_history (user_id, sender, message, matched, similarity, result)
        VALUES (%s, %s, %s, %s, %s, %s)
    """, (user_id, sender, message, matched, similarity, result))
    conn.commit()
    detection_id = cursor.lastrowid
    conn.close()
    return detection_id

def insert_report_history(user_id, detection_id):
    conn = pymysql.connect(**DB_CFG)
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO report_history (user_id, detection_id, reported_at)
        VALUES (%s, %s, NOW())
    """, (user_id, detection_id))
    conn.commit()
    id = cursor.lastrowid
    conn.close()
    return id

def get_detection_history(user_id):    
    conn = pymysql.connect(**DB_CFG)
    cursor = conn.cursor()
    cursor.execute("""
        SELECT received_at, sender, message
        FROM detection_history
        WHERE user_id = %s
        ORDER BY received_at DESC
        LIMIT 100
    """, (user_id,))
    rows = cursor.fetchall()
    conn.close()

    result = []
    for row in rows:
        received_at = row[0].strftime("%Y-%m-%d") if isinstance(row[0], datetime) else str(row[0])
        result.append({
            "received_at": received_at,
            "sender": row[1],
            "message": row[2]
        })

    return result

def get_report_history(user_id):
    conn = pymysql.connect(**DB_CFG)
    cursor = conn.cursor(pymysql.cursors.DictCursor)
    sql = """
        SELECT rh.reported_at, dh.sender, dh.message
        FROM report_history rh
        JOIN detection_history dh ON rh.detection_id = dh.id
        WHERE rh.user_id = %s
        ORDER BY rh.reported_at DESC
    """
    cursor.execute(sql, (user_id))
    result = cursor.fetchall()
    conn.close()

    # ✅ 날짜 포맷 yyyy-mm-dd로 자르기
    for row in result:
        if isinstance(row["reported_at"], datetime):
            row["reported_at"] = row["reported_at"].strftime("%Y-%m-%d")

    return result

