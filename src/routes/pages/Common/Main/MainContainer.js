import React, { useState, useEffect } from "react";
import MainPresenter from "./MainPresenter";

const MainContainer = () => {
    // 페이지 이동을 위한 외부 함수
    const [smishingData, setSmishingData] = useState(null);
    const [malwareLinkData, setMalwareLinkData] = useState(null);

    useEffect(() => {
        // TODO: 향후 API 호출로 대체될 부분
        // 1. 스미싱 데이터 (가짜 데이터)
        setSmishingData({
        stats: {
            activeUsers: 120,
            realtimeDetection: 3,
            realtimeBlock: 1,
            blockSuccessRate: 33.3,
            detectionRate: 5.0,
        },
        dailyTrend: [
            { day: "월", count: 280 },
            { day: "화", count: 180 },
            { day: "수", count: 320 },
            { day: "목", count: 120 },
            { day: "금", count: 350 },
            { day: "토", count: 150 },
            { day: "일", count: 220 },
        ],
        logs: [
            { time: "14:23:45", from: "010-xxxx", message: "[국세청] 환급금...", risk: "높음", action: "차단" },
            { time: "14:22:11", from: "010-yyyy", message: "[Web발신] 택배...", risk: "의심", action: "탐지" },
        ],
        });

        // 2. 악성링크 데이터 (가짜 데이터)
        setMalwareLinkData({
            stats: {
                activeUsers: 120,
                realtimeDetection: 1,
                realtimeBlock: 1,
                blockSuccessRate: 100.0,
                detectionRate: 2.1,
            },
            dailyTrend: [
                { day: "월", count: 280 },
                { day: "화", count: 180 },
                { day: "수", count: 320 },
                { day: "목", count: 120 },
                { day: "금", count: 350 },
                { day: "토", count: 150 },
                { day: "일", count: 220 },
            ],
            logs: [
                { time: "14:23:45", from: "010-xxxx", url: "https://short.ly/...", risk: "높음", action: "차단" },
            ],
        });

    }, []);

    // 데이터 로딩 중일 때 보여줄 화면 (선택 사항)
    if (!smishingData || !malwareLinkData) {
        return <div>Loading...</div>;
    }
    
    return (
        <MainPresenter
            smishingData={smishingData} 
            malwareLinkData={malwareLinkData} 
        />
    )
}

export default MainContainer;