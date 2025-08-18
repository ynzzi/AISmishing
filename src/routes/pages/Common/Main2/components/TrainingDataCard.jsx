import React from 'react';

const TrainingDataCard = ({ title, data, type }) => {
    return (
        <div className="main2-content-block">
        {/* 👇 이 부분을 h4에서 h3로 변경합니다. */}
        <h3>{title}</h3>
        <div className="training-card-content">
            <div className="training-info-item">
            <span>✓ 마지막 학습</span>
            <span>{data.lastDate}</span>
            </div>
            <div className="training-info-item">
            <span>✓ 다음 예정</span>
            <span>{data.nextDate}</span>
            </div>
            <div className="training-info-item">
            <span>✓ {type} 학습 데이터 수</span>
            <span>{data.count.toLocaleString()}건</span>
            </div>
        </div>
        </div>
    );
};

export default TrainingDataCard;