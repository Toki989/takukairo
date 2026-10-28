#!/bin/sh
# Finder does not inherit the user's interactive shell PATH.
PATH="$HOME/.docker/bin:/Applications/Docker.app/Contents/Resources/bin:/opt/homebrew/bin:/usr/local/bin:$PATH"
export PATH
if ! command -v node >/dev/null 2>&1; then
    printf '%s\n' 'Node.js 24をインストールしてください。手順: docs/MAC.md' >&2
    exit 1
fi
