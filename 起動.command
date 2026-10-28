#!/bin/sh
cd "$(dirname "$0")" || exit 1
. ./scripts/mac-env.sh
node scripts/mac-local.mjs start "$@"
result=$?
if [ "$result" -ne 0 ]; then
    printf '\n%s\n' '起動できませんでした。上の説明と docs/MAC.md を確認してください。Enterで閉じます。'
    read -r answer
fi
exit "$result"
