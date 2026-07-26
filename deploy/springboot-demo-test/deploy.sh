#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGE_ENV="/etc/springboot-demo-test/images.env"

[[ -f "${IMAGE_ENV}" ]] || {
    echo "缺少 ${IMAGE_ENV}，请先根据 images.env.example 创建" >&2
    exit 1
}

backend_image="$(awk -F= '$1 == "BACKEND_IMAGE" {sub(/^[^=]*=/, ""); print; exit}' "${IMAGE_ENV}")"
frontend_image="$(awk -F= '$1 == "FRONTEND_IMAGE" {sub(/^[^=]*=/, ""); print; exit}' "${IMAGE_ENV}")"

bash "${SCRIPT_DIR}/deploy-image.sh" backend "${backend_image}"
bash "${SCRIPT_DIR}/deploy-image.sh" frontend "${frontend_image}"
