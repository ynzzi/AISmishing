import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const DailyTrendChart = ({ trendData }) => {
    return (
        // ResponsiveContainer를 사용해 부모 요소의 크기에 맞춰 차트 크기를 조절합니다.
        <ResponsiveContainer width="100%" height={300}>
            <BarChart
                data={trendData}
                margin={{
                    top: 20,
                    right: 10,
                    left: 0,
                    bottom: 0,
                }}
            >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="count" fill="#8884d8" name="탐지 수" />
            </BarChart>
        </ResponsiveContainer>
    );
};

export default DailyTrendChart;