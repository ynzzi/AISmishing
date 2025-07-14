from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import re

# ✅ FastAPI 인스턴스 생성
app = FastAPI()

# ✅ CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],            # 모든 origin 허용 (앱에서 호출 가능)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ✅ 모델 경로 설정 (압축 해제한 모델 디렉토리 경로)
MODEL_PATH = "./ko_miniLM_spam_model"

# ✅ HuggingFace 토크나이저 및 모델 로드
tokenizer = AutoTokenizer.from_pretrained(MODEL_PATH)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_PATH)
model.eval()

# ✅ 입력 데이터 모델
class TextInput(BaseModel):
    text: str

# ✅ 루트 확인용
@app.get("/")
def root():
    return {"message": "스팸 분류 AI 서버 작동 중"}

# ✅ 벡터화 및 분류 요청 처리
@app.post("/analyze")
async def vectorize(input: TextInput):
    cleaned_text = re.sub(r'\s+', ' ', input.text.strip())  # 줄바꿈 제거 및 공백 정리
    inputs = tokenizer(cleaned_text, return_tensors="pt", truncation=True, padding=True)
    with torch.no_grad():
        outputs = model(**inputs)
        probs = torch.sigmoid(outputs.logits)
        score = probs[0][0].item()
        label = "normal" if score > 0.8 else "spam"
    return {"score": score, "result": label}


# from fastapi import FastAPI, Form, Query
# from pydantic import BaseModel
# from fastapi.middleware.cors import CORSMiddleware
# from transformers import AutoTokenizer, AutoModelForSequenceClassification
# import torch
# import re
# import pymysql
# import os
# from dotenv import load_dotenv
# from datetime import datetime

# # ✅ .env 로드
# load_dotenv()

# # ✅ DB 설정
# DB_CFG = {
#     "host":     os.getenv("DB_HOST", "localhost"),
#     "user":     os.getenv("DB_USER", "root"),
#     "password": os.getenv("DB_PASS", ""),
#     "db":       os.getenv("DB_NAME", "SMS"),
#     "charset":  os.getenv("DB_CHARSET", "utf8")
# }

# # ✅ FastAPI 인스턴스 생성
# app = FastAPI()

# # ✅ CORS 설정
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["*"],
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )

# # ✅ 모델 경로 설정
# MODEL_PATH = "./ko_miniLM_spam_model"

# # ✅ 모델 및 토크나이저 로드
# tokenizer = AutoTokenizer.from_pretrained(MODEL_PATH)
# model = AutoModelForSequenceClassification.from_pretrained(MODEL_PATH)
# model.eval()

# # ✅ 입력 데이터 클래스
# class TextInput(BaseModel):
#     text: str

# # ✅ 루트 확인용
# @app.get("/")
# def root():
#     return {"message": "스팸 분류 AI 서버 작동 중"}

# # ✅ 기존 벡터화 API (단순 판별)
# @app.post("/vectorize")
# async def vectorize(input: TextInput):
#     cleaned_text = re.sub(r'\s+', ' ', input.text.strip())
#     inputs = tokenizer(cleaned_text, return_tensors="pt", truncation=True, padding=True)
#     with torch.no_grad():
#         outputs = model(**inputs)
#         probs = torch.sigmoid(outputs.logits)
#         score = probs[0][0].item()
#         label = "normal" if score > 0.8 else "spam"
#     return {"score": score, "result": label}

# # ✅ 판별 + DB 저장
# @app.post("/sms")
# async def check_sms(user_id: str = Form(...), message: str = Form(...)):
#     cleaned_text = re.sub(r'\s+', ' ', message.strip())
#     inputs = tokenizer(cleaned_text, return_tensors="pt", truncation=True, padding=True)
#     with torch.no_grad():
#         outputs = model(**inputs)
#         probs = torch.sigmoid(outputs.logits)
#         score = probs[0][0].item()
#     result = "normal" if score > 0.8 else "spam"

#     detection_id = None
#     if result == "spam":
#         conn = pymysql.connect(**DB_CFG)
#         cursor = conn.cursor()
#         cursor.execute("""
#             INSERT INTO detection_history (user_id, message, score, result)
#             VALUES (%s, %s, %s, %s)
#         """, (user_id, message, score, result))
#         conn.commit()
#         detection_id = cursor.lastrowid
#         conn.close()

#     return {
#         "user_id": user_id,
#         "message": message,
#         "score": score,
#         "result": result,
#         "detection_id": detection_id
#     }

# # ✅ 사용자 신고 DB 저장
# @app.post("/report")
# async def report_sms(user_id: str = Form(...), detection_id: int = Form(...)):
#     conn = pymysql.connect(**DB_CFG)
#     cursor = conn.cursor()
#     cursor.execute("""
#         INSERT INTO report_history (user_id, detection_id, reported_at)
#         VALUES (%s, %s, NOW())
#     """, (user_id, detection_id))
#     conn.commit()
#     report_id = cursor.lastrowid
#     conn.close()
#     return {"report_id": report_id}

# # ✅ 탐지 이력 조회
# @app.get("/detection")
# async def get_detections(user_id: str = Query(...)):
#     conn = pymysql.connect(**DB_CFG)
#     cursor = conn.cursor()
#     cursor.execute("""
#         SELECT received_at, message, score, result
#         FROM detection_history
#         WHERE user_id = %s
#         ORDER BY received_at DESC
#         LIMIT 100
#     """, (user_id,))
#     rows = cursor.fetchall()
#     conn.close()
#     return [{
#         "received_at": row[0].strftime("%Y-%m-%d"),
#         "message": row[1],
#         "score": float(row[2]),
#         "result": row[3]
#     } for row in rows]

# # ✅ 신고 이력 조회
# @app.get("/report")
# async def get_reports(user_id: str = Query(...)):
#     conn = pymysql.connect(**DB_CFG)
#     cursor = conn.cursor()
#     cursor.execute("""
#         SELECT r.reported_at, d.message, d.result
#         FROM report_history r
#         JOIN detection_history d ON r.detection_id = d.id
#         WHERE r.user_id = %s
#         ORDER BY r.reported_at DESC
#     """, (user_id,))
#     rows = cursor.fetchall()
#     conn.close()
#     return [{
#         "reported_at": row[0].strftime("%Y-%m-%d"),
#         "message": row[1],
#         "result": row[2]
#     } for row in rows]
