-- CREATE DATABASE SMS;
use SMS;
-- CREATE TABLE sms_dataset (
--   id INT AUTO_INCREMENT PRIMARY KEY,
--   text TEXT NOT NULL,
--   label TINYINT(1) NOT NULL
-- ) ENGINE=MyISAM;

CREATE TABLE user (
  id VARCHAR(100) PRIMARY KEY,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20) NOT NULL UNIQUE,
  regdate DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO user (id, password, email, phone)
VALUES 
  ('user001', 'hashed_password1', 'user1@example.com', '01012345678'),
  ('user002', 'hashed_password2', 'user2@example.com', '01087654321');


CREATE TABLE detection_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,  -- 사용자 구분용 ID
    received_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sender VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    matched TEXT,
    similarity FLOAT,
    result ENUM('정상', '스팸'),
    FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO detection_history (user_id, sender, message, matched, similarity, result)
VALUES
  ('user001', '01099998888', '대출 광고입니다.', '대출 광고입니다.', 0.95, '스팸'),
  ('user002', '15881234', '[Web발신] 최신 아이폰 할인 이벤트! 지금 바로 신청하세요!', '[Web발신] 최신 아이폰 할인 이벤트! 지금 바로 신청하세요!', 0.88, '스팸');
  
  CREATE TABLE report_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,                   -- 사용자 ID (user.id 참조)
    detection_id INT NOT NULL,                       -- 어떤 탐지 기록을 신고했는지 (detection_history.id 참조)
    reported_at DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 신고 시각
    
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (detection_id) REFERENCES detection_history(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO report_history (user_id, detection_id)
VALUES
  ('user001', 1),
  ('user002', 2);
  
SELECT host, user FROM mysql.user;
SELECT user, host, plugin FROM mysql.user WHERE user = 'root';

show tables;

ALTER TABLE report_history
ADD UNIQUE KEY unique_report (user_id, detection_id);

INSERT INTO sms_dataset (text, label) VALUES
('이건 정상적인 메시지입니다.', 1),
('대출 해드립니다. 지금 바로 전화주세요.', 0),
('안녕하세요. 오늘 일정 확인 부탁드립니다.', 1),
('무료쿠폰 지급 이벤트 클릭!', 0);

SHOW VARIABLES LIKE 'secure_file_priv';


LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/final_dataset.csv'
INTO TABLE sms_dataset
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(text, label);

SHOW PROCESSLIST;

SHOW OPEN TABLES WHERE In_use > 0;

SHOW INDEX FROM report_history;

SELECT * FROM sms_detection WHERE user_id = 'user001';

SELECT * FROM report_history WHERE user_id = 'user001';

SELECT * FROM detection_history WHERE user_id = 'user001' ORDER BY id DESC;
SELECT * FROM report_history WHERE user_id = 'user001' ORDER BY id DESC;
