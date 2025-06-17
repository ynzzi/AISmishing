from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModel
import torch
from transformers import AutoModelForSequenceClassification

app = Flask(__name__)
tokenizer = AutoTokenizer.from_pretrained("BM-K/KoMiniLM", use_auth_token=False)
model = AutoModelForSequenceClassification.from_pretrained("BM-K/KoMiniLM", use_auth_token=False)

@app.route("/")
def index():
    return "Ko-MiniLM Vector Server is running!"

@app.route("/vectorize", methods=["POST"])
def vectorize():
    text = request.json.get("text", "")
    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True)
    with torch.no_grad():
        outputs = model(**inputs)
        cls_embedding = outputs.last_hidden_state[:, 0, :].squeeze().tolist()
    return jsonify({"vector": cls_embedding})
