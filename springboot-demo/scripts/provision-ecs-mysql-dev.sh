#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

: "${ECS_HOST:?Set ECS_HOST to the ECS public IP or an SSH host alias}"

ECS_USER="${ECS_USER:-root}"
ECS_SSH_PORT="${ECS_SSH_PORT:-22}"
DB_HOST="${DB_HOST:-${ECS_HOST}}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-my-mysql}"
ENV_FILE="${ENV_FILE:-${PROJECT_ROOT}/.env}"
ENV_EXAMPLE="${PROJECT_ROOT}/.env.example"
SCHEMA_FILE="${PROJECT_ROOT}/sql/20260717_create_demo_user.sql"

if [[ ! "${ECS_HOST}" =~ ^[A-Za-z0-9._:-]+$ ]]; then
    echo "ECS_HOST contains unsupported characters" >&2
    exit 1
fi

if [[ ! "${DB_HOST}" =~ ^[A-Za-z0-9._:-]+$ ]]; then
    echo "DB_HOST contains unsupported characters" >&2
    exit 1
fi

if [[ ! "${ECS_USER}" =~ ^[A-Za-z0-9._-]+$ ]]; then
    echo "ECS_USER contains unsupported characters" >&2
    exit 1
fi

if [[ ! "${ECS_SSH_PORT}" =~ ^[0-9]+$ ]]; then
    echo "ECS_SSH_PORT must be numeric" >&2
    exit 1
fi

if [[ ! "${MYSQL_CONTAINER}" =~ ^[A-Za-z0-9_.-]+$ ]]; then
    echo "MYSQL_CONTAINER contains unsupported characters" >&2
    exit 1
fi

if [[ -e "${ENV_FILE}" ]]; then
    echo "Refusing to overwrite existing ${ENV_FILE}" >&2
    exit 1
fi

for required_file in "${ENV_EXAMPLE}" "${SCHEMA_FILE}"; do
    if [[ ! -f "${required_file}" ]]; then
        echo "Required file not found: ${required_file}" >&2
        exit 1
    fi
done

for required_command in openssl ssh; do
    if ! command -v "${required_command}" >/dev/null 2>&1; then
        echo "Required command not found: ${required_command}" >&2
        exit 1
    fi
done

ssh_options=(
    -o BatchMode=yes
    -o ConnectTimeout=8
    -o StrictHostKeyChecking=yes
    -p "${ECS_SSH_PORT}"
)

run_mysql_as_root() {
    ssh "${ssh_options[@]}" "${ECS_USER}@${ECS_HOST}" \
        "docker exec -i ${MYSQL_CONTAINER} sh -c 'MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot'"
}

ssh "${ssh_options[@]}" "${ECS_USER}@${ECS_HOST}" \
    "docker inspect ${MYSQL_CONTAINER} >/dev/null && docker exec ${MYSQL_CONTAINER} mysql --version"

db_password="$(openssl rand -hex 24)"
env_tmp="$(mktemp "${PROJECT_ROOT}/.env.tmp.XXXXXX")"
trap 'rm -f "${env_tmp}"' EXIT
chmod 600 "${env_tmp}"

while IFS= read -r line || [[ -n "${line}" ]]; do
    case "${line}" in
        DB_URL=*)
            printf "DB_URL='jdbc:mysql://%s:3306/springboot_demo?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&sslMode=DISABLED&connectTimeout=5000&socketTimeout=10000&tcpKeepAlive=true'\n" "${DB_HOST}"
            ;;
        DB_PASSWORD=*)
            printf "DB_PASSWORD='%s'\n" "${db_password}"
            ;;
        ECS_HOST=*)
            printf "ECS_HOST='%s'\n" "${ECS_HOST}"
            ;;
        ECS_USER=*)
            printf "ECS_USER='%s'\n" "${ECS_USER}"
            ;;
        ECS_SSH_PORT=*)
            printf "ECS_SSH_PORT='%s'\n" "${ECS_SSH_PORT}"
            ;;
        *)
            printf '%s\n' "${line}"
            ;;
    esac
done < "${ENV_EXAMPLE}" > "${env_tmp}"

run_mysql_as_root < "${SCHEMA_FILE}"

{
    printf "CREATE USER IF NOT EXISTS 'springboot_app'@'%%' IDENTIFIED BY '%s';\n" "${db_password}"
    printf "ALTER USER 'springboot_app'@'%%' IDENTIFIED BY '%s';\n" "${db_password}"
    printf "GRANT SELECT, INSERT, UPDATE, DELETE ON springboot_demo.* TO 'springboot_app'@'%%';\n"
    printf "FLUSH PRIVILEGES;\n"
} | run_mysql_as_root

printf '%s\n' \
    "SELECT VERSION() AS mysql_version;" \
    "SELECT COUNT(*) AS demo_user_table_count" \
    "FROM information_schema.TABLES" \
    "WHERE TABLE_SCHEMA = 'springboot_demo' AND TABLE_NAME = 'demo_user';" \
    "SELECT COUNT(*) AS springboot_app_count" \
    "FROM mysql.user" \
    "WHERE User = 'springboot_app';" | run_mysql_as_root

mv "${env_tmp}" "${ENV_FILE}"
chmod 600 "${ENV_FILE}"
trap - EXIT
unset db_password

echo "Provisioned springboot_demo and springboot_app on ${MYSQL_CONTAINER}"
echo "Wrote local development settings to ${ENV_FILE}"
echo "The generated database password was not printed"
