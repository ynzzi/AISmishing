import pymysql
from db_loader import get_conn
from typing import Optional, Dict, Any

def get_device_by_token(device_token: str) -> Optional[Dict[str, Any]]:
    sql = """
        SELECT user_device_id, user_id, device_id, device_token, token_revoked
        FROM user_devices
        WHERE device_token = %s
        LIMIT 1
    """
    with get_conn() as conn, conn.cursor(pymysql.cursors.DictCursor) as cur:
        cur.execute(sql, (device_token,))
        return cur.fetchone()

def upsert_device_with_token(user_id: str, device_id: str, device_token: str) -> None:
    sql = """
        INSERT INTO user_devices (user_id, device_id, device_token, last_seen_at)
        VALUES (%s, %s, %s, CURRENT_TIMESTAMP)
        ON DUPLICATE KEY UPDATE
            device_token = VALUES(device_token),
            token_revoked = 0,
            last_seen_at = CURRENT_TIMESTAMP
    """
    with get_conn() as conn, conn.cursor(pymysql.cursors.DictCursor) as cur:
        cur.execute(sql, (user_id, device_id, device_token))

def touch_device_seen(user_device_id: int) -> None:
    sql = "UPDATE user_devices SET last_seen_at = CURRENT_TIMESTAMP WHERE user_device_id = %s"
    with get_conn() as conn, conn.cursor(pymysql.cursors.DictCursor) as cur:
        cur.execute(sql, (user_device_id,))

def revoke_device_token(user_device_id: int) -> None:
    sql = "UPDATE user_devices SET token_revoked = 1 WHERE user_device_id = %s"
    with get_conn() as conn, conn.cursor(pymysql.cursors.DictCursor) as cur:
        cur.execute(sql, (user_device_id,))
        