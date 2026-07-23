#!/usr/bin/env bash

set -euo pipefail

: "${ECS_HOST:?Set ECS_HOST to the ECS public IP or an SSH host alias}"

ECS_USER="${ECS_USER:-root}"
ECS_SSH_PORT="${ECS_SSH_PORT:-22}"
LOCAL_MYSQL_PORT="${LOCAL_MYSQL_PORT:-13306}"
REMOTE_MYSQL_HOST="${REMOTE_MYSQL_HOST:-127.0.0.1}"
REMOTE_MYSQL_PORT="${REMOTE_MYSQL_PORT:-3306}"

if ! command -v ssh >/dev/null 2>&1; then
    echo "ssh command not found" >&2
    exit 1
fi

echo "Opening ECS MySQL tunnel on 127.0.0.1:${LOCAL_MYSQL_PORT}"
echo "Press Ctrl+C to close the tunnel"

exec ssh \
    -N \
    -o ExitOnForwardFailure=yes \
    -o ServerAliveInterval=60 \
    -o ServerAliveCountMax=3 \
    -p "${ECS_SSH_PORT}" \
    -L "127.0.0.1:${LOCAL_MYSQL_PORT}:${REMOTE_MYSQL_HOST}:${REMOTE_MYSQL_PORT}" \
    "${ECS_USER}@${ECS_HOST}"
