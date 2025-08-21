import secrets

def new_device_token() -> str:
    # 충돌 확률 매우 낮은 32~48바이트 추천
    return secrets.token_urlsafe(48)