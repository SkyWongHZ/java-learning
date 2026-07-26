#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MYSQL_ROOT="$(mktemp -d /tmp/springboot-demo-mysql.XXXXXX)"
MYSQL_DATA="${MYSQL_ROOT}/data"
MYSQL_SOCKET="${MYSQL_ROOT}/mysql.sock"
MYSQL_PORT=3307
MYSQL_PID=""
REDIS_PID=""

cleanup() {
    if [[ -n "${REDIS_PID}" ]] && kill -0 "${REDIS_PID}" 2>/dev/null; then
        kill "${REDIS_PID}" 2>/dev/null || true
        wait "${REDIS_PID}" 2>/dev/null || true
    fi
    if [[ -n "${MYSQL_PID}" ]] && kill -0 "${MYSQL_PID}" 2>/dev/null; then
        kill "${MYSQL_PID}" 2>/dev/null || true
        wait "${MYSQL_PID}" 2>/dev/null || true
    fi
    rm -rf -- "${MYSQL_ROOT}"
}
trap cleanup EXIT

install_test_services() {
    if command -v mariadbd >/dev/null 2>&1 \
        && command -v redis-server >/dev/null 2>&1; then
        return
    fi

    command -v dnf >/dev/null 2>&1 || {
        echo "构建环境缺少 dnf，无法安装临时 MySQL/Redis 测试服务" >&2
        exit 1
    }

    dnf install -y mariadb-server redis \
        || dnf install -y mariadb105-server redis6
}

install_test_services

MYSQLD="$(command -v mariadbd || command -v mysqld)"
MYSQL_INSTALL_DB="$(command -v mariadb-install-db || command -v mysql_install_db)"
MYSQL_CLIENT="$(command -v mariadb || command -v mysql)"
MYSQL_ADMIN="$(command -v mariadb-admin || command -v mysqladmin)"

mkdir -p "${MYSQL_DATA}"
"${MYSQL_INSTALL_DB}" \
    --datadir="${MYSQL_DATA}" \
    --auth-root-authentication-method=normal \
    --skip-test-db >/dev/null

"${MYSQLD}" \
    --datadir="${MYSQL_DATA}" \
    --socket="${MYSQL_SOCKET}" \
    --port="${MYSQL_PORT}" \
    --bind-address=127.0.0.1 \
    --pid-file="${MYSQL_ROOT}/mysql.pid" \
    --log-error="${MYSQL_ROOT}/mysql.log" \
    --skip-name-resolve \
    --user="$(id -un)" &
MYSQL_PID=$!

for _ in $(seq 1 60); do
    if "${MYSQL_ADMIN}" \
        --protocol=tcp \
        --host=127.0.0.1 \
        --port="${MYSQL_PORT}" \
        --user=root ping --silent >/dev/null 2>&1; then
        break
    fi
    if ! kill -0 "${MYSQL_PID}" 2>/dev/null; then
        cat "${MYSQL_ROOT}/mysql.log" >&2 || true
        echo "临时 MySQL 启动失败" >&2
        exit 1
    fi
    sleep 1
done

"${MYSQL_ADMIN}" \
    --protocol=tcp \
    --host=127.0.0.1 \
    --port="${MYSQL_PORT}" \
    --user=root ping --silent >/dev/null

"${MYSQL_CLIENT}" \
    --protocol=tcp \
    --host=127.0.0.1 \
    --port="${MYSQL_PORT}" \
    --user=root \
    <"${PROJECT_ROOT}/springboot-demo/sql/20260717_create_demo_user.sql"
"${MYSQL_CLIENT}" \
    --protocol=tcp \
    --host=127.0.0.1 \
    --port="${MYSQL_PORT}" \
    --user=root \
    <"${PROJECT_ROOT}/springboot-demo/sql/20260723_create_auth_tables.sql"
"${MYSQL_CLIENT}" \
    --protocol=tcp \
    --host=127.0.0.1 \
    --port="${MYSQL_PORT}" \
    --user=root \
    springboot_demo \
    <"${PROJECT_ROOT}/springboot-demo/sql/20260726_create_student_management_tables.sql"

redis-server \
    --bind 127.0.0.1 \
    --port 6379 \
    --save "" \
    --appendonly no \
    --daemonize no \
    >"${MYSQL_ROOT}/redis.log" 2>&1 &
REDIS_PID=$!

for _ in $(seq 1 30); do
    if redis-cli -h 127.0.0.1 -p 6379 ping 2>/dev/null | grep -qx PONG; then
        break
    fi
    if ! kill -0 "${REDIS_PID}" 2>/dev/null; then
        cat "${MYSQL_ROOT}/redis.log" >&2 || true
        echo "临时 Redis 启动失败" >&2
        exit 1
    fi
    sleep 1
done
redis-cli -h 127.0.0.1 -p 6379 ping | grep -qx PONG

export SPRING_PROFILES_ACTIVE=test
export DB_URL="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/springboot_demo?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&sslMode=DISABLED"
export DB_USERNAME=root
export DB_PASSWORD=""
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=""
export REDIS_DATABASE=15

cd "${PROJECT_ROOT}/springboot-demo"
chmod +x mvnw
./mvnw -B clean verify
