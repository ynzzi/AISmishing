import React from "react";
import DashboardSection from "./components/DashboardSection";
import { Link } from "react-router-dom";
import './Main.css';
import Header from "../../../../components/Header/Header";

const MainPresenter = ({
    smishingData,
    malwareLinkData
}) => {

    return (
        <div className="dashboard-wrapper">
            <Header />

            <div className="dashboard-container">
                <DashboardSection
                title="안티 스미싱 실시간 현황"
                data={smishingData}
                logType="message" // 로그 테이블의 종류를 구분하기 위한 prop
                />
                <DashboardSection
                title="안티 악성링크 실시간 현황"
                data={malwareLinkData}
                logType="url" // 로그 테이블의 종류를 구분하기 위한 prop
                />
            </div>

            <div className="nav-button-container">
                <Link to="/main2">
                <button className="nav-button">성능 확인</button>
                </Link>
            </div>
        </div>
    )
}

export default MainPresenter;