#!/usr/bin/env sh
set -eu

escape() {
  printf '%s' "$1" | sed 's/[&|]/\\&/g'
}

# URL 끝의 / 를 떼서 "__X__/path" 형태로 이어 붙여도 // 가 생기지 않게 한다.
trim_slash() {
  printf '%s' "$1" | sed 's|/*$||'
}

embedded_server_host="${EMBEDDED_SERVER_HOST:?EMBEDDED_SERVER_HOST is required}"

# blackbox probe 대상은 prod 설정에만 있다. dev 설정에는 placeholder가 없으므로 비어 있어도 된다.
this_server_url=$(trim_slash "${PROD_THIS_SERVER_URL:-}")
grafana_host=$(trim_slash "${PROD_GRAFANA_HOST:-}")
ai_server_host=$(trim_slash "${AI_SERVER_HOST:-}")

sed \
  -e "s|__EMBEDDED_SERVER_HOST__|$(escape "$embedded_server_host")|g" \
  -e "s|__PROD_THIS_SERVER_URL__|$(escape "$this_server_url")|g" \
  -e "s|__PROD_GRAFANA_HOST__|$(escape "$grafana_host")|g" \
  -e "s|__AI_SERVER_HOST__|$(escape "$ai_server_host")|g" \
  /etc/prometheus/prometheus.yml \
  > /tmp/prometheus.yml

exec /bin/prometheus --config.file=/tmp/prometheus.yml "$@"
