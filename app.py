from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModel
import torch
import os

app = Flask(__name__)

# ✅ 모델 및 토크나이저 로딩 (공개 모델)
model_name = "BM-K/KoMiniLM"
tokenizer = AutoTokenizer.from_pretrained(model_name, use_auth_token=False)
model = AutoModel.from_pretrained(model_name)

@app.route("/")
def index():
    return "✅ KoMiniLM Vector Server is running!"

@app.route("/vectorize", methods=["POST"])
def vectorize():
    data = request.get_json()
    text = data.get("text", "")

    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True)
    with torch.no_grad():
        outputs = model(**inputs)
        cls_embedding = outputs.last_hidden_state[:, 0, :]  # [CLS] 벡터
        vector = cls_embedding.squeeze().tolist()

    return jsonify({"vector": vector})

# ✅ Render에서 필요한 포트 바인딩
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 10000))
    app.run(host="0.0.0.0", port=port)
