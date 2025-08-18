import React from 'react';
import './RealtimeLogTable.css'; // 테이블 스타일

const RealtimeLogTable = ({ logs, logType }) => {
    // logType에 따라 테이블 헤더 이름을 결정합니다.
    const dynamicHeader = logType === 'message' ? '메시지 요약' : 'URL';

    return (
        <div className="table-container">
            <table>
                <thead>
                    <tr>
                        <th>시간</th>
                        <th>발신번호</th>
                        <th>{dynamicHeader}</th>
                        <th>위험도</th>
                        <th>조치</th>
                    </tr>
                </thead>
                <tbody>
                    {logs.map((log, index) => (
                        <tr key={index}>
                            <td>{log.time}</td>
                            <td>{log.from}</td>
                            {/* logType에 따라 해당 데이터를 보여줍니다. */}
                            <td className="log-content">{logType === 'message' ? log.message : log.url}</td>
                            <td>
                                <span className={`risk-level ${log.risk === '높음' ? 'high' : 'medium'}`}>
                                    {log.risk}
                                </span>
                            </td>
                            <td>{log.action}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default RealtimeLogTable;