#!/usr/bin/env bash

set -Eeuo pipefail

die() {
    echo "部署失败：$*" >&2
    exit 1
}

log() {
    printf '[%s] %s\n' "$(date '+%F %T')" "$*"
}

read_image_value() {
    local key="$1"
    awk -F= -v key="${key}" '$1 == key {sub(/^[^=]*=/, ""); print; exit}' "${IMAGE_ENV}"
}

write_image_values() {
    local backend_image="$1"
    local frontend_image="$2"
    local tmp_file

    tmp_file="$(mktemp "${ENV_DIR}/images.env.XXXXXX")"
    {
        printf 'BACKEND_IMAGE=%s\n' "${backend_image}"
        printf 'FRONTEND_IMAGE=%s\n' "${frontend_image}"
    } >"${tmp_file}"
    chmod 0600 "${tmp_file}"
    mv -f "${tmp_file}" "${IMAGE_ENV}"
}

compose() {
    DOCKER_CONFIG="${DOCKER_CONFIG_DIR}" docker compose \
        --env-file "${IMAGE_ENV}" \
        --project-directory "${COMPOSE_DIR}" \
        -f "${COMPOSE_FILE}" "$@"
}

container_health() {
    docker inspect \
        --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
        "${CONTAINER_NAME}" 2>/dev/null || true
}

wait_for_health() {
    local status

    for _ in $(seq 1 75); do
        status="$(container_health)"
        if [[ "${status}" == "healthy" ]]; then
            return 0
        fi
        if [[ "${status}" == "exited" || "${status}" == "dead" ]]; then
            return 1
        fi
        sleep 2
    done
    return 1
}

verify_service() {
    wait_for_health || return 1

    if [[ "${SERVICE}" == "backend" ]]; then
        docker exec "${CONTAINER_NAME}" \
            wget -q -T 3 -O /dev/null http://127.0.0.1:18763/hello

        if docker inspect springboot-demo-test-frontend >/dev/null 2>&1; then
            curl --fail --silent --show-error --max-time 8 \
                http://127.0.0.1/doc.html >/dev/null
        fi
    else
        curl --fail --silent --show-error --max-time 8 \
            http://127.0.0.1/operation/ >/dev/null
        curl --fail --silent --show-error --max-time 8 \
            http://127.0.0.1/doc.html >/dev/null
    fi
}

rollback() {
    local rollback_compose_file="${COMPOSE_FILE}"
    local rollback_compose_dir="${COMPOSE_DIR}"

    [[ "${ROLLBACK_REQUIRED}" == "true" ]] || return 0
    ROLLBACK_REQUIRED=false

    log "${SERVICE} 健康检查失败，开始恢复上一镜像：${PREVIOUS_IMAGE}"
    write_image_values "${PREVIOUS_BACKEND_IMAGE}" "${PREVIOUS_FRONTEND_IMAGE}"

    if [[ -n "${PREVIOUS_RELEASE}" && -f "${PREVIOUS_RELEASE}/compose.yaml" ]]; then
        rollback_compose_file="${PREVIOUS_RELEASE}/compose.yaml"
        rollback_compose_dir="${PREVIOUS_RELEASE}"
    fi

    DOCKER_CONFIG="${DOCKER_CONFIG_DIR}" docker compose \
        --env-file "${IMAGE_ENV}" \
        --project-directory "${rollback_compose_dir}" \
        -f "${rollback_compose_file}" \
        up -d --no-deps "${SERVICE}" || true

    if [[ -n "${PREVIOUS_RELEASE}" ]]; then
        ln -sfn "${PREVIOUS_RELEASE}" "${CURRENT_LINK}"
    fi

    docker logs --tail=150 "${CONTAINER_NAME}" >&2 || true
    return 0
}

[[ "${EUID}" -eq 0 ]] || die "请使用 root 执行部署"

SERVICE="${1:-}"
NEW_IMAGE="${2:-}"

case "${SERVICE}" in
    backend)
        IMAGE_KEY="BACKEND_IMAGE"
        CONTAINER_NAME="springboot-demo-test-backend"
        ;;
    frontend)
        IMAGE_KEY="FRONTEND_IMAGE"
        CONTAINER_NAME="springboot-demo-test-frontend"
        ;;
    *)
        die "服务名必须是 backend 或 frontend"
        ;;
esac

[[ "${NEW_IMAGE}" =~ ^[A-Za-z0-9._:/@-]+$ ]] || die "非法镜像地址：${NEW_IMAGE:-<empty>}"

: "${ACR_REGISTRY:?缺少 ACR_REGISTRY}"
: "${ACR_USERNAME:?缺少 ACR_USERNAME}"
: "${ACR_PASSWORD:?缺少 ACR_PASSWORD}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="${SCRIPT_DIR}"
COMPOSE_FILE="${COMPOSE_DIR}/compose.yaml"
APP_ROOT="/opt/springboot-demo-test"
RELEASES_DIR="${APP_ROOT}/releases"
CURRENT_LINK="${APP_ROOT}/current"
ENV_DIR="/etc/springboot-demo-test"
IMAGE_ENV="${ENV_DIR}/images.env"
RUNTIME_ENV="${ENV_DIR}/runtime.env"
DATA_NETWORK="springboot-demo-data"
LOCK_FILE="/var/lock/springboot-demo-test-deploy.lock"
DOCKER_CONFIG_DIR=""
ROLLBACK_REQUIRED=false

for command in docker curl flock awk; do
    command -v "${command}" >/dev/null 2>&1 || die "缺少命令：${command}"
done
docker compose version >/dev/null 2>&1 || die "Docker Compose v2 不可用"

[[ -f "${COMPOSE_FILE}" ]] || die "缺少 ${COMPOSE_FILE}"
[[ -f "${RUNTIME_ENV}" ]] || die "缺少 ${RUNTIME_ENV}"
if grep -Eq 'replace_me|your-ecs-public-ip' "${RUNTIME_ENV}"; then
    die "${RUNTIME_ENV} 仍包含模板占位值"
fi

install -d -m 0750 "${ENV_DIR}"
install -d -m 0755 "${APP_ROOT}" "${RELEASES_DIR}"

exec 9>"${LOCK_FILE}"
flock -w 300 9 || die "等待另一个发布任务结束超时"

docker inspect my-mysql >/dev/null 2>&1 || die "找不到 my-mysql 容器"
docker inspect springboot-demo-redis >/dev/null 2>&1 \
    || die "找不到 springboot-demo-redis 容器"

if ! docker network inspect "${DATA_NETWORK}" >/dev/null 2>&1; then
    docker network create "${DATA_NETWORK}" >/dev/null
fi

for container in my-mysql springboot-demo-redis; do
    if ! docker network inspect "${DATA_NETWORK}" \
        --format '{{range .Containers}}{{.Name}}{{"\n"}}{{end}}' \
        | grep -Fxq "${container}"; then
        docker network connect "${DATA_NETWORK}" "${container}"
    fi
done

if [[ ! -f "${IMAGE_ENV}" ]]; then
    current_backend="$(docker inspect --format '{{.Config.Image}}' \
        springboot-demo-test-backend 2>/dev/null || true)"
    current_frontend="$(docker inspect --format '{{.Config.Image}}' \
        springboot-demo-test-frontend 2>/dev/null || true)"
    [[ -n "${current_backend}" ]] || die "无法确定当前后端镜像"
    [[ -n "${current_frontend}" ]] || die "无法确定当前前端镜像"
    write_image_values "${current_backend}" "${current_frontend}"
fi

PREVIOUS_BACKEND_IMAGE="$(read_image_value BACKEND_IMAGE)"
PREVIOUS_FRONTEND_IMAGE="$(read_image_value FRONTEND_IMAGE)"
[[ -n "${PREVIOUS_BACKEND_IMAGE}" ]] || die "${IMAGE_ENV} 缺少 BACKEND_IMAGE"
[[ -n "${PREVIOUS_FRONTEND_IMAGE}" ]] || die "${IMAGE_ENV} 缺少 FRONTEND_IMAGE"

if [[ "${IMAGE_KEY}" == "BACKEND_IMAGE" ]]; then
    PREVIOUS_IMAGE="${PREVIOUS_BACKEND_IMAGE}"
    next_backend_image="${NEW_IMAGE}"
    next_frontend_image="${PREVIOUS_FRONTEND_IMAGE}"
else
    PREVIOUS_IMAGE="${PREVIOUS_FRONTEND_IMAGE}"
    next_backend_image="${PREVIOUS_BACKEND_IMAGE}"
    next_frontend_image="${NEW_IMAGE}"
fi

PREVIOUS_RELEASE=""
if [[ -L "${CURRENT_LINK}" ]]; then
    PREVIOUS_RELEASE="$(readlink -f "${CURRENT_LINK}" || true)"
fi

DOCKER_CONFIG_DIR="$(mktemp -d /tmp/springboot-demo-docker-config.XXXXXX)"
cleanup() {
    rm -rf -- "${DOCKER_CONFIG_DIR}"
}
trap cleanup EXIT

printf '%s' "${ACR_PASSWORD}" \
    | DOCKER_CONFIG="${DOCKER_CONFIG_DIR}" docker login \
        --username "${ACR_USERNAME}" \
        --password-stdin "${ACR_REGISTRY}" >/dev/null

write_image_values "${next_backend_image}" "${next_frontend_image}"
ROLLBACK_REQUIRED=true
trap rollback ERR

compose config --quiet
log "拉取 ${SERVICE} 镜像：${NEW_IMAGE}"
compose pull "${SERVICE}"
compose up -d --no-deps "${SERVICE}"

if ! verify_service; then
    compose ps >&2 || true
    compose logs --tail=150 "${SERVICE}" >&2 || true
    false
fi

ln -sfn "${SCRIPT_DIR}" "${CURRENT_LINK}"
ROLLBACK_REQUIRED=false
trap - ERR

log "${SERVICE} 测试环境发布成功"
compose ps "${SERVICE}"
