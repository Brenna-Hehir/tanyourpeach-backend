CREATE DATABASE IF NOT EXISTS tanyourpeach_test
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

GRANT ALL PRIVILEGES ON tanyourpeach_test.* TO 'springboot'@'%';

FLUSH PRIVILEGES;