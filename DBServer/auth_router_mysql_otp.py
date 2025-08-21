# auth_router_mysql_otp.py
# (uvicorn main:app 로 실행할 때 기준)

from fastapi import APIRouter, HTTPException, Form, status, Request
from pydantic import BaseModel
from typing import Optional
import os
import re
import bcrypt
import pymysql

from security import create_access_token
from utils_tokens import new_device_token
from db_loader import get_conn
from device_repo import upsert_device_with_token

router = APIRouter(prefix="/auth", tags=["auth"])

# ===== OTP (데모) =====
class VerifyOtpPayload(BaseModel):
    phone: str
    otp: str
    device_id: Optional[str] = None

DEBUG = os.getenv("DEBUG", "false").lower() == "true"

def _verify_otp_in_db(phone: str, otp: str) -> dict:
    if DEBUG and otp == "000000":
        return {"user_id": f"user:{phone}"}
    if not otp or len(otp) < 4:
        raise HTTPException(status_code=400, detail="잘못된 OTP")
    # TODO: 실제 OTP 검증 로직
    return {"user_id": f"user:{phone}"}

@router.post("/otp/verify")
def verify_otp(payload: VerifyOtpPayload):
    user_row = _verify_otp_in_db(payload.phone, payload.otp)
    user_id = user_row["user_id"]

    # 들어온 device_id 로깅
    did = (payload.device_id or "").strip() or None
    print(f"[otp/verify] user_id={user_id} device_id={did}")

    device_token = None
    if did:
        try:
            device_token = new_device_token()
            upsert_device_with_token(user_id=user_id, device_id=did, device_token=device_token)
            print(f"[otp/verify] upsert OK (user_id={user_id}, device_id={did})")
        except Exception as e:
            print(f"[otp/verify] upsert FAILED: {e}")

    access_token = create_access_token(sub=payload.phone, uid=user_id)
    return {"access_token": access_token, "token_type": "bearer", "device_token": device_token}


# ===== 로그인 =====
def _get_user_and_hash(user_id: str):
    """
    DB 스키마:
      table: `user`
      cols : id, password, email, phone, regdate
    """
    sql = "SELECT id, password FROM `user` WHERE id=%s LIMIT 1"
    with get_conn() as conn, conn.cursor() as cur:
        cur.execute(sql, (user_id,))
        row = cur.fetchone()
        if not row:
            return None
        try:
            return {"id": row["id"], "password": row["password"]}
        except Exception:
            return {"id": row[0], "password": row[1]}

def _is_bcrypt_hash(val: bytes) -> bool:
    return val.startswith(b"$2")  # $2a/$2b/$2y

@router.post("/login")
async def login(
    request: Request,
    username: Optional[str] = Form(None),
    password: Optional[str] = Form(None),
    device_id: Optional[str] = Form(None),
):
    # 1) 우선 Form
    if username is None or password is None:
        # 2) Form 아니면 JSON
        try:
            data = await request.json()
        except Exception:
            data = {}
        username = data.get("username")
        password = data.get("password")
        device_id = data.get("device_id")

    if not username or not password:
        raise HTTPException(status_code=400, detail="username/password 누락")

    user_id = username.strip()
    raw_pw = password
    did = (device_id or "").strip() or None

    print(f"[login] in: user_id={user_id} device_id={did!r}")

    user = _get_user_and_hash(user_id)
    if not user:
        raise HTTPException(status_code=401, detail="아이디 또는 비밀번호가 올바르지 않습니다.")

    stored = user["password"]
    hash_bytes = stored if isinstance(stored, bytes) else str(stored).encode("utf-8")

    ok = False
    migrated = False
    try:
        if _is_bcrypt_hash(hash_bytes):
            ok = bcrypt.checkpw(raw_pw.encode("utf-8"), hash_bytes)
        else:
            ok = (raw_pw == hash_bytes.decode("utf-8"))
            if ok:
                new_hash = bcrypt.hashpw(raw_pw.encode("utf-8"), bcrypt.gensalt()).decode()
                with get_conn() as conn, conn.cursor() as cur:
                    cur.execute("UPDATE `user` SET `password`=%s WHERE id=%s", (new_hash, user_id))
                migrated = True
    except Exception as e:
        print(f"[login] password check error: {e}")
        ok = False

    if not ok:
        raise HTTPException(status_code=401, detail="아이디 또는 비밀번호가 올바르지 않습니다.")

    device_token = None
    if did:
        try:
            device_token = new_device_token()
            upsert_device_with_token(user_id=user_id, device_id=did, device_token=device_token)
            print(f"[login] upsert OK (user_id={user_id}, device_id={did})")
        except Exception as e:
            # 여기에서 실패 사유가 보이면 DB/스키마 문제 가능성 높음
            print(f"[login] upsert FAILED: {e}")

    access_token = create_access_token(sub=user_id, uid=user_id)
    return {
        "access_token": access_token,
        "token_type": "bearer",
        "device_token": device_token,  # did가 있을 때만 값
        "migrated": migrated,
    }


# ===== 아이디 중복 확인 =====
@router.get("/check-username")
def check_username(username: str):
    uid = username.strip()
    sql = "SELECT 1 FROM `user` WHERE id=%s LIMIT 1"
    try:
        with get_conn() as conn, conn.cursor() as cur:
            cur.execute(sql, (uid,))
            exists = cur.fetchone() is not None
            return {"available": not exists}
    except pymysql.err.ProgrammingError as e:
        print(f"[check-username] SQL error: {e}")
        raise HTTPException(status_code=500, detail="SQL error (table/column 확인)")
    except Exception as e:
        print(f"[check-username] Unexpected error: {e}")
        raise HTTPException(status_code=500, detail="Internal error")


# ===== 회원가입 =====
EMAIL_RE = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")

@router.post("/signup", status_code=status.HTTP_201_CREATED)
def signup(
    username: str = Form(...),
    password: str = Form(...),
    phone: str = Form(...),
    email: str = Form(...),
    device_id: Optional[str] = Form(None),
):
    username = username.strip()
    phone = phone.strip()
    email = email.strip()
    did = (device_id or "").strip() or None

    if len(username) < 3 or len(username) > 100:
        raise HTTPException(400, detail="아이디 길이가 유효하지 않습니다.")
    if len(password) < 4:
        raise HTTPException(400, detail="비밀번호는 4자 이상이어야 합니다.")
    if not EMAIL_RE.match(email):
        raise HTTPException(400, detail="이메일 형식이 유효하지 않습니다.")

    pw_hash = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt()).decode()

    try:
        with get_conn() as conn, conn.cursor() as cur:
            cur.execute("SELECT 1 FROM `user` WHERE id=%s LIMIT 1", (username,))
            if cur.fetchone():
                raise HTTPException(status_code=409, detail="이미 존재하는 아이디입니다.")

            cur.execute(
                """
                INSERT INTO `user` (id, password, phone, email)
                VALUES (%s, %s, %s, %s)
                """,
                (username, pw_hash, phone, email),
            )
            # autocommit=True 이면 commit 불필요

        device_token = None
        if did:
            try:
                device_token = new_device_token()
                upsert_device_with_token(user_id=username, device_id=did, device_token=device_token)
                print(f"[signup] upsert OK (user_id={username}, device_id={did})")
            except Exception as e:
                print(f"[signup] upsert FAILED: {e}")

        return {"ok": True, "device_token": device_token}

    except pymysql.err.IntegrityError as e:
        print(f"[signup] IntegrityError: {e}")
        raise HTTPException(status_code=409, detail="중복된 정보가 있습니다.") from e
    except pymysql.err.ProgrammingError as e:
        print(f"[signup] SQL error: {e}")
        raise HTTPException(status_code=500, detail="SQL error (table/column 확인)") from e
    except Exception as e:
        print(f"[signup] Unexpected error: {e}")
        raise HTTPException(status_code=500, detail="Internal error") from e
