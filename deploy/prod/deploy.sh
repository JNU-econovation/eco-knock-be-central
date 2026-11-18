#!/usr/bin/env sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
repo_root="$(CDPATH= cd -- "$script_dir/../.." && pwd)"

command="${1:-up}"
if [ "$#" -gt 0 ]; then
    shift
fi

build_option_set=false
for arg in "$@"; do
    case "$arg" in
        --build|--no-build)
            build_option_set=true
            ;;
    esac
done

cd "$repo_root"
git pull origin main

chmod +x "$script_dir/prod.sh"

# CI가 GHCR에 올린 이미지를 APP_IMAGE로 넘기면 서버에서 빌드하지 않고 그 이미지를 받아 실행한다.
if [ "$command" = "up" ] && [ -n "${APP_IMAGE:-}" ]; then
    export APP_IMAGE
    "$script_dir/prod.sh" pull app
    "$script_dir/prod.sh" up --no-build "$@"
elif [ "$command" = "up" ] && [ "$build_option_set" = false ]; then
    "$script_dir/prod.sh" "$command" --build "$@"
else
    "$script_dir/prod.sh" "$command" "$@"
fi
