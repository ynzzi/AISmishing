import React, { useState, useEffect } from "react";
// 👇 Presenter 임포트 경로 및 이름 수정
import Main2Presenter from "./Main2Presenter";

// 👇 컨테이너 컴포넌트 이름 수정
const Main2Container = () => {
    const [performanceData, setPerformanceData] = useState(null);
    const [trainingData, setTrainingData] = useState(null);

    useEffect(() => {
        // 데이터 로직은 동일
        setPerformanceData({
        metrics: { accuracy: 98.7, precision: 97.2, recall: 99.1, f1Score: 98.1, responseTime: 0.08, },
        trend: [ { day: "1주차", count: 180 }, { day: "2주차", count: 280 }, { day: "3주차", count: 220 }, { day: "4주차", count: 340 }, { day: "5주차", count: 190 }, { day: "6주차", count: 240 }, { day: "7주차", count: 210 }, ],
        });
        setTrainingData({
        smishing: { lastDate: "2025.07.10", nextDate: "2025.07.25", count: 12345, },
        malware: { lastDate: "2025.07.20", nextDate: "2025.07.15", count: 5432, },
        });
    }, []);

    if (!performanceData || !trainingData) {
        return <div>Loading...</div>;
    }

    // 👇 Presenter 컴포넌트를 props와 함께 렌더링
    return (
        <Main2Presenter
        performanceData={performanceData}
        trainingData={trainingData}
        />
    );
};

// 👇 export 이름 수정
export default Main2Container;