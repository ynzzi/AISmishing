import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import TrainingDataCard from './components/TrainingDataCard';
import './Main2.css';
import Header from "../../../../components/Header/Header";

// 👇 컴포넌트 이름 및 CSS 클래스 이름 수정 (d2- -> main2-)
const Main2Presenter = ({ performanceData, trainingData }) => {
    const { metrics, trend } = performanceData;

    return (
        <div className="main2-wrapper">
        <Header />
        
        <section className="main2-section">
            <div className="main2-content-block metrics-block">
                <div className="performance-container">


                    <div className="main2-content-block metrics-block">
                        <h3>Ko-miniLM-small 성능 지표</h3>

                        <div className="metrics-grid">
                            <div className="metric-item">
                                <span className="metric-label">✓ Accuracy</span>
                                <span className="metric-value">{metrics.accuracy.toFixed(1)}%</span>
                            </div>
                            <div className="metric-item">
                                <span className="metric-label">✓ Precision</span>
                                <span className="metric-value">{metrics.precision.toFixed(1)}%</span>
                            </div>
                            <div className="metric-item">
                                <span className="metric-label">✓ Recall</span>
                                <span className="metric-value">{metrics.recall.toFixed(1)}%</span>
                            </div>
                            <div className="metric-item">
                                <span className="metric-label">✓ F1-Score</span>
                                <span className="metric-value">{metrics.f1Score.toFixed(1)}%</span>
                            </div>
                            <div className="metric-item">
                                <span className="metric-label">✓ 응답시간</span>
                                <span className="metric-value">{metrics.responseTime.toFixed(2)}초</span>
                            </div>
                        </div>
                    </div>

                    <div className="main2-content-block chart-block">
                        <h3>성능 추이 (지난 30일)</h3>
                        <ResponsiveContainer width="100%" height={350}>
                        <BarChart data={trend}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="day" />
                            <YAxis />
                            <Tooltip />
                            <Bar dataKey="count" fill="#8884d8" />
                        </BarChart>
                        </ResponsiveContainer>
                    </div>
                    
                </div>
            </div>
        </section>

        <section className="main2-section">
            <h2 className='section-main-title'>학습 데이터 관리</h2>
            <div className='training-cards-container'>
                <TrainingDataCard title="스미싱 학습 데이터" data={trainingData.smishing} type="문자" />
                <TrainingDataCard title="악성링크 학습 데이터" data={trainingData.malware} type="URL" />
            </div>
        </section>
        </div>
    );
};

export default Main2Presenter;