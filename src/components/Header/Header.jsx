import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import SigninModal from '../SignIn/SigninModal.jsx';
import './Header.css'; // Header 전용 CSS 파일을 import 합니다.

const Header = () => {

    const [isModalOpen, setIsModalOpen] = useState(false);

    const openModal = () => {
        setIsModalOpen(true);
    };
    const closeModal = () => {
        setIsModalOpen(false);
    };

    return (
        <>
            <header className="main-header">
                <Link to="/" className="app-name-link">
                    <span className="app-name">어플 이름</span>
                </Link>
                <button className="login-button" onClick={openModal}>
                    로그인
                </button>
            </header>

            <SigninModal isOpen={isModalOpen} onClose={closeModal} />
        </>
    );
};

export default Header;