import React from 'react';

import StatsDisplay from './StatsDisplay';
import DailyTrendChart from './DailyTrendChart';
import RealtimeLogTable from './RealtimeLogTable';

const DashboardSection = ({ title, data, logType }) => {

    return (
        <div className="dashboard-section">
            <h2 className="section-title">{title}</h2>

            <div className="section-content">
                <div className="content-block stats-block">
                    <h3>탐지/차단 현황</h3>
                    <StatsDisplay stats={data.stats} />
                </div>
                <div className="content-block chart-block">
                    <h3>일일 탐지 추이</h3>
                    <DailyTrendChart trendData={data.dailyTrend} />
                </div>
            </div>

            <div className="log-section">
                <h3>실시간 탐지/차단 로그 (Top5)</h3>
                <RealtimeLogTable logs={data.logs} logType={logType} />
            </div>
        </div>
    );
}

export default DashboardSection;