#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${ENV_FILE:-${PROJECT_ROOT}/.env}"

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

if [[ "${SPRING_PROFILES_ACTIVE}" != "dev" ]]; then
    echo "scripts/start-dev.sh requires SPRING_PROFILES_ACTIVE=dev" >&2
    exit 1
fi

if [[ -n "${java_home}" ]]; then
    export JAVA_HOME="${java_home}"
    export PATH="${JAVA_HOME}/bin:${PATH}"
fi

cd "${PROJECT_ROOT}"
echo "Starting springboot-demo with Java 17 and the dev profile"
exec "${PROJECT_ROOT}/mvnw" spring-boot:run "$@"
