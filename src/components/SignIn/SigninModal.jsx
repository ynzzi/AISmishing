import React, { useState } from 'react';
import ReactDOM from 'react-dom';
import './SigninModal.css';
import logoImage from '../../assets/logo.png';

const SigninModal = ({ isOpen, onClose }) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    // 모달이 열려있지 않으면 아무것도 렌더링하지 않습니다.
    if (!isOpen) return null;

    const handleLogin = (e) => {
        e.preventDefault();
        // TODO: 실제 로그인 로직 구현
        console.log('Email:', email, 'Password:', password);
        alert('로그인 시도!');
        onClose(); // 로그인 시도 후 모달 닫기
    };

    // Portal을 사용하여 modal-root에 렌더링합니다.
    return ReactDOM.createPortal(
        <div className="modal-backdrop" onClick={onClose}>
        <div className="modal-content" onClick={e => e.stopPropagation()}>
            {/* 왼쪽 이미지 섹션 */}
            <div className="modal-image-section">
                <img src={logoImage} alt="로고 이미지" />
            </div>

            {/* 오른쪽 폼 섹션 */}
            <div className="modal-form-section">
            <h2>Welcome!</h2>
            <form onSubmit={handleLogin}>
                <div className="form-group">
                <label htmlFor="email">Email Address</label>
                <input
                    type="email"
                    id="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />
                </div>
                <div className="form-group">
                <label htmlFor="password">Password</label>
                <input
                    type="password"
                    id="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                </div>
                <button type="submit" className="modal-login-button">Sign in</button>
            </form>
            </div>
        </div>
        </div>,
        document.getElementById('modal-root')
    );
};

export default SigninModal;