# db_loader.py
import os
from contextlib import contextmanager
from datetime import datetime

import pymysql
from dotenv import load_dotenv

load_dotenv()

DB_CFG = {
    "host":     os.getenv("DB_HOST", "localhost"),
    "user":     os.getenv("DB_USER", "root"),
    "password": os.getenv("DB_PASS", "tjddbswl"),      # 환경변수명 유지
    "database": os.getenv("DB_NAME", "aismishingdb"),  # PyMySQL는 'database' 키 사용
    "charset":  os.getenv("DB_CHARSET", "utf8mb4"),    # ← utf8mb4 권장
    "autocommit": True,
    "cursorclass": pymysql.cursors.Cursor,             # 필요 시 DictCursor로 교체
}

@contextmanager
def get_conn(dict_cursor: bool = False):
    cfg = DB_CFG.copy()
    if dict_cursor:
        cfg["cursorclass"] = pymysql.cursors.DictCursor
    conn = pymysql.connect(**cfg)
    try:
        yield conn
    finally:
        conn.close()

# =========================
# Dataset (TF-IDF) 로딩
# =========================
def load_dataset(limit=None):
    query = "SELECT text, label FROM sms_dataset"
    if limit:
        query += " LIMIT %s"

    with get_conn() as conn, conn.cursor() as cur:
        if limit:
            cur.execute(query, (int(limit),))
        else:
            cur.execute(query)
        rows = cur.fetchall()

    texts  = [row[0] for row in rows]
    labels = [row[1] for row in rows]

    # Vectorizer는 그대로 유지
    from sklearn.feature_extraction.text import TfidfVectorizer
    vectorizer = TfidfVectorizer()
    matrix = vectorizer.fit_transform(texts)

    return texts, labels, vectorizer, matrix

# =========================
# 탐지 이력 / 신고 이력
# =========================
def insert_detection_history(user_id, sender, message, matched, similarity, result):
    """
    detection_history 스키마 예시(가정):
      id BIGINT AI PK
      user_id VARCHAR(100)  -- FK 아님이어도 됨
      sender VARCHAR(...)
      message TEXT
      matched VARCHAR(...) NULL
      similarity DOUBLE NULL
      result VARCHAR(10)   -- '스팸'/'정상'
      received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    """
    sql = """
        INSERT INTO detection_history
            (user_id, sender, message, matched, similarity, result)
        VALUES
            (%s, %s, %s, %s, %s, %s)
    """
    with get_conn() as conn, conn.cursor() as cur:
        cur.execute(sql, (user_id, sender, message, matched, similarity, result))
        return cur.lastrowid

def insert_report_history(user_id, detection_id):
    """
    report_history 스키마 예시(가정):
      id BIGINT AI PK
      user_id VARCHAR(100)
      detection_id BIGINT
      reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    """
    sql = """
        INSERT INTO report_history (user_id, detection_id, reported_at)
        VALUES (%s, %s, NOW())
    """
    with get_conn() as conn, conn.cursor() as cur:
        cur.execute(sql, (user_id, detection_id))  # ← (user_id,) 아님에 주의
        return cur.lastrowid

def get_detection_history(user_id):
    """
    가장 최근 100건. received_at은 문자열 yyyy-mm-dd 로 반환.
    """
    sql = """
        SELECT received_at, sender, message
          FROM detection_history
         WHERE user_id = %s
         ORDER BY received_at DESC
         LIMIT 100
    """
    with get_conn() as conn, conn.cursor() as cur:
        cur.execute(sql, (user_id,))
        rows = cur.fetchall()

    result = []
    for row in rows:
        received_at_raw = row[0]
        received_at = (
            received_at_raw.strftime("%Y-%m-%d")
            if isinstance(received_at_raw, datetime)
            else str(received_at_raw)
        )
        result.append(
            {
                "received_at": received_at,
                "sender": row[1],
                "message": row[2],
            }
        )
    return result

def get_report_history(user_id):
    """
    DictCursor로 키 기반 접근. reported_at은 yyyy-mm-dd 로 포맷.
    """
    sql = """
        SELECT rh.reported_at, dh.sender, dh.message
          FROM report_history rh
          JOIN detection_history dh ON rh.detection_id = dh.id
         WHERE rh.user_id = %s
         ORDER BY rh.reported_at DESC
    """
    with get_conn(dict_cursor=True) as conn, conn.cursor() as cur:
        cur.execute(sql, (user_id,))   # ← 튜플로 바인딩
        rows = cur.fetchall()

    for row in rows:
        ra = row.get("reported_at")
        if isinstance(ra, datetime):
            row["reported_at"] = ra.strftime("%Y-%m-%d")
        else:
            row["reported_at"] = str(ra)
    return rows
