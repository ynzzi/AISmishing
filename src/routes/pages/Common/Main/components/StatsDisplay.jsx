import React from 'react';
import './StatsDisplay.css'; // 스타일링을 위한 CSS 파일

const StatsDisplay = ({ stats }) => {
    // props로 받은 stats 객체를 사용하기 쉽게 구조 분해 할당합니다.
    const { 
        activeUsers, 
        realtimeDetection, 
        realtimeBlock, 
        blockSuccessRate, 
        detectionRate 
    } = stats;

    return (
        <div className="stats-grid">
            <div className="stat-item">
                <span className="stat-label">✓ 활성 사용자</span>
                <span className="stat-value">{activeUsers.toLocaleString()}명</span>
            </div>
            <div className="stat-item">
                <span className="stat-label">✓ 실시간 탐지</span>
                <span className="stat-value">{realtimeDetection.toLocaleString()} / 시간</span>
            </div>
            <div className="stat-item">
                <span className="stat-label">✓ 실시간 차단</span>
                <span className="stat-value">{realtimeBlock.toLocaleString()} / 시간</span>
            </div>
            <div className="stat-item">
                <span className="stat-label">✓ 차단 성공률</span>
                <span className="stat-value">{blockSuccessRate.toFixed(1)}%</span>
            </div>
            <div className="stat-item">
                <span className="stat-label">✓ 오탐율</span>
                <span className="stat-value">{detectionRate.toFixed(1)}%</span>
            </div>
        </div>
    );
};

export default StatsDisplay;