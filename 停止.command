#!/bin/sh
cd "$(dirname "$0")" || exit 1
. ./scripts/mac-env.sh
node scripts/mac-local.mjs stop "$@"
result=$?
if [ "$result" -ne 0 ]; then
    printf '\n%s\n' '停止を完了できませんでした。上の説明を確認してください。Enterで閉じます。'
    read -r answer
fi
exit "$result"
