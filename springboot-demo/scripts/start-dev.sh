#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${ENV_FILE:-${PROJECT_ROOT}/.env}"
run_mode="dev"

case "${1:-}" in
    --test)
        run_mode="test"
        shift
        ;;
    --help|-h)
        echo "Usage:"
        echo "  ./scripts/start-dev.sh          Start the application with the dev profile"
        echo "  ./scripts/start-dev.sh --test   Run all tests with the test profile"
        exit 0
        ;;
esac

if [[ ! -f "${ENV_FILE}" ]]; then
    echo "Environment file not found: ${ENV_FILE}" >&2
    echo "Copy .env.example to .env and fill in the local values first." >&2
    exit 1
fi

java_home="${SPRINGBOOT_JAVA_HOME:-${JAVA_HOME:-}}"
if [[ -z "${java_home}" ]]; then
    for candidate in /opt/homebrew/opt/openjdk@17 /usr/local/opt/openjdk@17; do
        if [[ -x "${candidate}/bin/java" ]]; then
            java_home="${candidate}"
            break
        fi
    done
fi

if [[ -n "${java_home}" ]]; then
    java_executable="${java_home}/bin/java"
else
    java_executable="$(command -v java || true)"
fi

if [[ -z "${java_executable}" || ! -x "${java_executable}" ]]; then
    echo "Java was not found. Install Java 17 or set SPRINGBOOT_JAVA_HOME." >&2
    exit 1
fi

java_version="$("${java_executable}" -version 2>&1)"
java_version="${java_version%%$'\n'*}"
if [[ "${java_version}" != *'"17.'* ]]; then
    echo "Java 17 is required, but the selected executable reports: ${java_version}" >&2
    exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

: "${SPRING_PROFILES_ACTIVE:?Set SPRING_PROFILES_ACTIVE=dev in ${ENV_FILE}}"
: "${DB_URL:?Set DB_URL in ${ENV_FILE}}"
: "${DB_USERNAME:?Set DB_USERNAME in ${ENV_FILE}}"
: "${DB_PASSWORD:?Set DB_PASSWORD in ${ENV_FILE}}"
: "${REDIS_HOST:?Set REDIS_HOST in ${ENV_FILE}}"
: "${REDIS_PORT:?Set REDIS_PORT in ${ENV_FILE}}"
: "${REDIS_PASSWORD:?Set REDIS_PASSWORD in ${ENV_FILE}}"

if [[ "${run_mode}" == "dev" && "${SPRING_PROFILES_ACTIVE}" != "dev" ]]; then
    echo "scripts/start-dev.sh requires SPRING_PROFILES_ACTIVE=dev" >&2
    exit 1
fi

for required_command in redis-cli ssh; do
    if ! command -v "${required_command}" >/dev/null 2>&1; then
        echo "Required command not found: ${required_command}" >&2
        exit 1
    fi
done

if [[ -n "${java_home}" ]]; then
    export JAVA_HOME="${java_home}"
    export PATH="${JAVA_HOME}/bin:${PATH}"
fi

redis_ready() {
    REDISCLI_AUTH="${REDIS_PASSWORD}" redis-cli \
        -h "${REDIS_HOST}" \
        -p "${REDIS_PORT}" \
        -n "${REDIS_DATABASE:-0}" \
        --no-auth-warning \
        PING 2>/dev/null | grep -qx PONG
}

redis_tunnel_pid=""
cleanup() {
    if [[ -n "${redis_tunnel_pid}" ]] && kill -0 "${redis_tunnel_pid}" 2>/dev/null; then
        kill "${redis_tunnel_pid}" 2>/dev/null || true
        wait "${redis_tunnel_pid}" 2>/dev/null || true
        echo "Redis SSH tunnel closed"
    fi
}
trap cleanup EXIT

if redis_ready; then
    echo "Redis is available at ${REDIS_HOST}:${REDIS_PORT}"
else
    : "${ECS_HOST:?Redis is unavailable; set ECS_HOST in ${ENV_FILE} to open the SSH tunnel}"

    ECS_USER="${ECS_USER:-root}"
    ECS_SSH_PORT="${ECS_SSH_PORT:-22}"
    LOCAL_REDIS_PORT="${LOCAL_REDIS_PORT:-${REDIS_PORT}}"
    REMOTE_REDIS_HOST="${REMOTE_REDIS_HOST:-127.0.0.1}"
    REMOTE_REDIS_PORT="${REMOTE_REDIS_PORT:-16379}"

    if [[ "${REDIS_HOST}" != "127.0.0.1" && "${REDIS_HOST}" != "localhost" ]]; then
        echo "Redis at ${REDIS_HOST}:${REDIS_PORT} is unavailable and cannot use a local SSH tunnel" >&2
        exit 1
    fi
    if [[ "${LOCAL_REDIS_PORT}" != "${REDIS_PORT}" ]]; then
        echo "LOCAL_REDIS_PORT must match REDIS_PORT" >&2
        exit 1
    fi
    if [[ ! "${ECS_HOST}" =~ ^[A-Za-z0-9._:-]+$
        || ! "${ECS_USER}" =~ ^[A-Za-z0-9._-]+$
        || ! "${REMOTE_REDIS_HOST}" =~ ^[A-Za-z0-9._:-]+$ ]]; then
        echo "Redis SSH tunnel settings contain unsupported characters" >&2
        exit 1
    fi
    for port in "${ECS_SSH_PORT}" "${LOCAL_REDIS_PORT}" "${REMOTE_REDIS_PORT}"; do
        if [[ ! "${port}" =~ ^[0-9]+$ ]]; then
            echo "SSH and Redis ports must be numeric" >&2
            exit 1
        fi
    done

    echo "Opening Redis SSH tunnel on 127.0.0.1:${LOCAL_REDIS_PORT}"
    ssh \
        -N \
        -o BatchMode=yes \
        -o ExitOnForwardFailure=yes \
        -o ServerAliveInterval=30 \
        -o ServerAliveCountMax=3 \
        -o StrictHostKeyChecking=yes \
        -p "${ECS_SSH_PORT}" \
        -L "127.0.0.1:${LOCAL_REDIS_PORT}:${REMOTE_REDIS_HOST}:${REMOTE_REDIS_PORT}" \
        "${ECS_USER}@${ECS_HOST}" &
    redis_tunnel_pid=$!

    for attempt in {1..20}; do
        if redis_ready; then
            echo "Redis SSH tunnel is ready"
            break
        fi
        if ! kill -0 "${redis_tunnel_pid}" 2>/dev/null; then
            wait "${redis_tunnel_pid}" || true
            echo "Redis SSH tunnel exited before Redis became available" >&2
            exit 1
        fi
        if [[ "${attempt}" -eq 20 ]]; then
            echo "Redis did not become available through the SSH tunnel" >&2
            exit 1
        fi
        sleep 0.5
    done
fi

cd "${PROJECT_ROOT}"
if [[ "${run_mode}" == "test" ]]; then
    export SPRING_PROFILES_ACTIVE=test
    echo "Running tests with Java 17, the test profile, MySQL, and Redis"
    "${PROJECT_ROOT}/mvnw" clean test "$@"
else
    echo "Starting springboot-demo with Java 17 and the dev profile"
    "${PROJECT_ROOT}/mvnw" spring-boot:run "$@"
fi
