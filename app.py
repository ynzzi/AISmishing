from flask import Flask, request, jsonify
import torch
from transformers import AutoTokenizer

app = Flask(__name__)

# ✅ 모델 및 토크나이저 불러오기
model_path = "ko_miniLM_mobile.pt"  # 같은 디렉토리에 있어야 함
tokenizer_name = "BM-K/KoMiniLM"  # HuggingFace에서 tokenizer 다운로드

# 토크나이저 로드 (로컬 또는 HuggingFace에서)
tokenizer = AutoTokenizer.from_pretrained(tokenizer_name)

# TorchScript 모델 로드 (torch.jit.trace 또는 torch.jit.script로 저장된 모델)
model = torch.jit.load(model_path)
model.eval()

@app.route("/")
def root():
    return "AI 모델 서버 정상 작동 중"

@app.route("/vectorize", methods=["POST"])
def vectorize():
    try:
        data = request.get_json()
        text = data.get("text", "")

        if not text.strip():
            return jsonify({"error": "텍스트 입력이 비어 있습니다."}), 400

        # 입력 토크나이즈
        inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)

        # 모델 추론
        with torch.no_grad():
            output = model(**inputs)

        # 예: sigmoid로 스미싱 확률 계산 (이진 분류 모델 기준)
        score = torch.sigmoid(output.logits)[0][0].item()
        return jsonify({"score": score})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=10000)
