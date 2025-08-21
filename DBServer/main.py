# main.py (최종 수정본)

from fastapi import FastAPI, Form, Header, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional, Dict, Any
from auth_router_mysql_otp import router as auth_router

from db_loader import (
    load_dataset,
    insert_detection_history,
    insert_report_history,
    get_detection_history,
    get_report_history,
)
from model import classify_sms

# JWT 디코드 (없으면 무시)
try:
    from security import decode_token
except Exception:
    decode_token = None

# 기기 토큰 조회
try:
    from device_repo import get_device_by_token, touch_device_seen
except Exception:
    def get_device_by_token(token: str):
        return None
    def touch_device_seen(_id: int):
        return None

app = FastAPI(title="SMS Guard API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],      # X-Device-Token 포함
)

# ===== 전역 분류 리소스 =====
texts, labels, tfidf_vectorizer, tfidf_matrix = [], [], None, None

@app.on_event("startup")
def startup_event():
    global texts, labels, tfidf_vectorizer, tfidf_matrix
    print("🚀 서버 시작 - 데이터셋 로드 중")
    texts, labels, tfidf_vectorizer, tfidf_matrix = load_dataset(limit=10000)
    print("✅ 로딩 완료")


# ===== 공통 인증 해석기 =====
def resolve_actor(
    authorization: Optional[str] = Header(None),
    # ✅ convert_underscores=False 제거 → 'x-device-token' 헤더가 자동 매핑됨
    x_device_token: Optional[str] = Header(None, alias="X-Device-Token"),
) -> Dict[str, Any]:
    """
    반환 예:
      {"mode": "user",   "id": 123}                       # JWT 기반 (users.id)
      {"mode": "device", "id": 123, "device": {...}}      # 기기토큰 기반 (users.id)
    """
    # 1) Bearer JWT 우선 (decode_token 제공 시)
    if decode_token and authorization and authorization.lower().startswith("bearer "):
        try:
            token = authorization.split(" ", 1)[1]
            payload = decode_token(token)
            # ✅ 토큰 페이로드에 users.id(PK)가 들어있어야 함
            if "id" not in payload:
                raise HTTPException(status_code=401, detail="토큰에 id가 없습니다.")
            return {"mode": "user", "id": payload["id"]}
        except HTTPException:
            raise
        except Exception:
            raise HTTPException(status_code=401, detail="유효하지 않은 액세스 토큰")

    # 2) 기기 토큰
    if x_device_token:
        # 디버그 로그 (필요시 주석 처리)
        print("X-Device-Token =", x_device_token[:16] + "…")

        dev = get_device_by_token(x_device_token)
        if not dev:
            raise HTTPException(status_code=401, detail="유효하지 않은 기기 토큰")
        if dev.get("token_revoked"):
            raise HTTPException(status_code=401, detail="토큰이 취소되었습니다.")

        # ✅ device_repo가 보통 아래 컬럼을 반환:
        #   user_device_id, user_id, device_id, device_token, token_revoked
        # users.id(PK)를 의미하는 값이 'user_id' 키로 들어오는 경우가 대부분이므로 우선 사용
        user_pk = dev.get("user_id") or dev.get("id")
        if user_pk is None:
            raise HTTPException(status_code=500, detail="기기-유저 매핑에 id가 없습니다.")

        # 마지막 접속 갱신
        if "user_device_id" in dev:
            try:
                touch_device_seen(dev["user_device_id"])
            except Exception:
                pass

        return {"mode": "device", "id": user_pk, "device": dev}

    # 3) 인증 없음
    raise HTTPException(status_code=401, detail="인증 정보가 없습니다. (Bearer JWT 또는 X-Device-Token 필요)")


# ===== SMS 분류 =====
@app.post("/sms")
def check_sms(
    sender: str = Form(...),
    message: str = Form(...),
    actor: Dict[str, Any] = Depends(resolve_actor),
):
    user_id = actor["id"]  # ✅ users.id(PK)

    print(f"📩 수신된 메시지 | by={actor['mode']} uid={user_id} | sender={sender} | msg={message[:60]}")

    result = classify_sms(
        message=message,
        texts=texts,
        labels=labels,
        tfidf_vectorizer=tfidf_vectorizer,
        tfidf_matrix=tfidf_matrix,
    )

    detection_id = None
    if result.get("result") == "스팸":
        detection_id = insert_detection_history(
            user_id=user_id,   # ✅ PK로 저장
            sender=sender,
            message=message,
            matched=result.get("matched"),
            similarity=result.get("similarity"),
            result=result.get("result"),
        )

    return {
        "sender": sender,
        "message": message,
        **result,
        "detection_id": detection_id,
    }


# ===== 탐지 이력 조회 =====
@app.get("/detection")
def get_detections(actor: Dict[str, Any] = Depends(resolve_actor)):
    user_id = actor["id"]  # ✅ PK 사용
    return get_detection_history(user_id)


# ===== 신고 등록 =====
@app.post("/report")
def report_sms(
    detection_id: int = Form(...),
    actor: Dict[str, Any] = Depends(resolve_actor),
):
    user_id = actor["id"]  # ✅ PK 사용
    print(f"📣 신고 요청 | uid={user_id} | detection_id={detection_id}")
    report_id = insert_report_history(user_id=user_id, detection_id=detection_id)
    return {"report_id": report_id}


# ===== 신고 이력 조회 =====
@app.get("/report")
def get_reports(actor: Dict[str, Any] = Depends(resolve_actor)):
    user_id = actor["id"]  # ✅ PK 사용
    return get_report_history(user_id)


# 인증 라우터 포함
app.include_router(auth_router)

@app.get("/whoami")
def whoami(actor: Dict[str, Any] = Depends(resolve_actor)):
    # 헤더가 제대로 오면 여기까지 들어옵니다.
    return actor