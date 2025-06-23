# main.py
from fastapi import FastAPI, Form, Query
from fastapi.middleware.cors import CORSMiddleware
from db_loader import load_dataset, insert_detection_history, insert_report_history, get_detection_history, get_report_history
from model import classify_sms

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

texts, labels, tfidf_vectorizer, tfidf_matrix = [], [], None, None

@app.on_event("startup")
def startup_event():
    global texts, labels, tfidf_vectorizer, tfidf_matrix
    print("🚀 서버 시작 - 데이터셋 로드 중")
    texts, labels, tfidf_vectorizer, tfidf_matrix = load_dataset(limit=10000)
    print("✅ 로딩 완료")

@app.post("/sms")
def check_sms(user_id: str = Form(...), sender: str = Form(...), message: str = Form(...)):
    print(f"📩 수신된 메시지\n- 발신자: {sender}\n- 내용: {message}")

    result = classify_sms(
        message=message,
        texts=texts,
        labels=labels,
        tfidf_vectorizer=tfidf_vectorizer,
        tfidf_matrix=tfidf_matrix,
    )

    detection_id = None
    if result["result"] == "스팸":
        detection_id = insert_detection_history(
            user_id=user_id,
            sender=sender,
            message=message,
            matched=result["matched"],
            similarity=result["similarity"],
            result=result["result"]
        )

    return {
        "sender": sender,
        "message": message,
        **result,
        "detection_id": detection_id,
    }

@app.get("/detection")
def get_detections(user_id: str = Query(...)):
    result = get_detection_history(user_id)

    return result

@app.post("/report")
def report_sms(user_id: str = Form(...), detection_id: int = Form(...)):
    print(f"📩 신고된 메시지\n- 유저: {user_id}")

    report_id = None
    report_id = insert_report_history(
            user_id=user_id,
            detection_id=detection_id
        )
    
    return {
        "report_id": report_id,
    }

@app.get("/report")
def get_reports(user_id: str = Query(...)):
    result = get_report_history(user_id)

    return result