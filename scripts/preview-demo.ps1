param([switch]$CheckOnly)
$ErrorActionPreference = 'Stop'
$workspace = Split-Path $PSScriptRoot -Parent
$nodeDirectory = Join-Path $workspace '.tools/node/node-v24.21.0-win-x64'
if (Test-Path (Join-Path $nodeDirectory 'node.exe')) { $env:PATH = "$nodeDirectory;$env:PATH" }
if (-not (Get-Command node.exe -ErrorAction SilentlyContinue)) { throw 'Node.js 24をインストールしてください。詳しくはdocs/BROWSER_DEMO.mdを参照してください。' }
Push-Location (Join-Path $workspace 'frontend')
$server = $null
try {
    if (-not (Test-Path 'node_modules/vite/bin/vite.js')) {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw '依存パッケージの取得に失敗しました。' }
    }
    & npm.cmd run build:demo
    if ($LASTEXITCODE -ne 0) { throw '体験版のビルドに失敗しました。' }
    $runtime = Join-Path $workspace '.runtime'
    New-Item -ItemType Directory -Force -Path $runtime | Out-Null
    $vite = Join-Path $workspace 'frontend/node_modules/vite/bin/vite.js'
    $node = (Get-Command node.exe).Source
    $server = Start-Process -FilePath $node -ArgumentList @(('"{0}"' -f $vite), 'preview', '--mode', 'demo', '--host', 'localhost', '--port', '4175', '--strictPort') -WorkingDirectory (Get-Location).Path -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $runtime 'demo-preview.log') -RedirectStandardError (Join-Path $runtime 'demo-preview-error.log')
    $ready = $false
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        Start-Sleep -Milliseconds 250
        $server.Refresh()
        if ($server.HasExited) { throw '体験版を起動できません。4175番ポートの使用状況と.runtime/demo-preview-error.logを確認してください。' }
        try {
            $response = Invoke-WebRequest -Uri 'http://localhost:4175/' -UseBasicParsing -TimeoutSec 1
            $html = [Text.Encoding]::UTF8.GetString($response.RawContentStream.ToArray())
            if ($response.StatusCode -eq 200 -and $html.Contains('ブラウザ体験版')) { $ready = $true; break }
        } catch { }
    }
    if (-not $ready) { throw '体験版の起動確認がタイムアウトしました。' }
    if ($CheckOnly) { Write-Output 'Demo launcher check passed: http://localhost:4175/'; return }
    Start-Process -FilePath 'http://localhost:4175/'
    Write-Host ''
    Write-Host '卓回廊 ブラウザ体験版: http://localhost:4175/'
    Write-Host 'ログイン・Java・データベースは不要です。'
    Read-Host '体験版を終了するには、この画面でEnterを押してください' | Out-Null
} finally {
    if ($server) { $server.Refresh(); if (-not $server.HasExited) { Stop-Process -Id $server.Id -ErrorAction SilentlyContinue } }
    Pop-Location
}
