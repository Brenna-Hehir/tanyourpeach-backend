#!/usr/bin/env bash

# The official MySQL image runs files in /docker-entrypoint-initdb.d only when
# the data volume is initialized for the first time.
(
  set -euo pipefail

  test_database="${MYSQL_TEST_DATABASE:-tanyourpeach_test}"
  app_user="${MYSQL_USER:-springboot}"

  MYSQL_PWD="${MYSQL_ROOT_PASSWORD}" mysql --protocol=socket -uroot <<SQL
CREATE DATABASE IF NOT EXISTS \`${test_database}\`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

GRANT ALL PRIVILEGES ON \`${test_database}\`.* TO '${app_user}'@'%';
FLUSH PRIVILEGES;
SQL
)