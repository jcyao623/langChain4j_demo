CREATE DATABASE IF NOT EXISTS langchain4j_demo
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'chat_app'@'%' IDENTIFIED BY 'chat_app123';
GRANT ALL PRIVILEGES ON langchain4j_demo.* TO 'chat_app'@'%';
FLUSH PRIVILEGES;

USE langchain4j_demo;

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    model_name VARCHAR(64) DEFAULT NULL,
    prompt_tokens INT DEFAULT NULL,
    completion_tokens INT DEFAULT NULL,
    total_tokens INT DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_session_created (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 客服聊天消息表';
