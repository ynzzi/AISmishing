# model.py
from sklearn.metrics.pairwise import cosine_similarity

def classify_sms(message, texts, labels, tfidf_vectorizer, tfidf_matrix, threshold=0.6):
    message_vector = tfidf_vectorizer.transform([message])
    cosine_sim = cosine_similarity(message_vector, tfidf_matrix)

    best_idx = cosine_sim.argmax()
    best_score = float(cosine_sim[0][best_idx])
    best_match = texts[best_idx]
    label = labels[best_idx]

    if best_score >= threshold:
        result = "스팸" if label == 1 else "정상"
        match_status = "매칭됨"
    else:
        result = "정상"
        match_status = "매칭 없음"

    return {
        "matched": best_match if best_score >= threshold else None,
        "similarity": round(best_score, 3),
        "match_status": match_status,
        "result": result
    }
