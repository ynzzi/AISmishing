# security.py
import os
import time
import jwt
from typing import Dict, Any

JWT_SECRET = os.getenv("JWT_SECRET", "dev-secret")
JWT_ALG = "HS256"
JWT_EXPIRE_MINUTES = int(os.getenv("JWT_EXPIRE_MINUTES", "30"))

def create_access_token(sub: str, uid: str | int) -> str:
    now = int(time.time())
    exp = now + JWT_EXPIRE_MINUTES * 60
    payload: Dict[str, Any] = {"sub": sub, "uid": uid, "iat": now, "exp": exp}
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALG)

def decode_token(token: str) -> Dict[str, Any]:
    return jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALG])
