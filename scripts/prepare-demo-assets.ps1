$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$workspace = Split-Path $PSScriptRoot -Parent
$destination = Join-Path $workspace 'frontend/public/demo-assets'
New-Item -ItemType Directory -Force -Path $destination | Out-Null
$jpeg = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() | Where-Object MimeType -eq 'image/jpeg'
$parameters = New-Object System.Drawing.Imaging.EncoderParameters(1)
$parameters.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter([System.Drawing.Imaging.Encoder]::Quality, [long]82)
$manifest = @{ scenarios = @(); pcs = @() }
$index = 0
foreach ($file in Get-ChildItem -LiteralPath (Join-Path $workspace 'シナリオトレーラー画像テスト用asset') -Filter '*.png' | Sort-Object Name) {
    $index++
    $name = 'scenario-{0:d2}.jpg' -f $index
    $source = [System.Drawing.Image]::FromFile($file.FullName)
    try {
        $scale = [Math]::Min(1, 1200 / [Math]::Max($source.Width, $source.Height))
        $bitmap = New-Object System.Drawing.Bitmap([int]($source.Width * $scale), [int]($source.Height * $scale))
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graphics.DrawImage($source, 0, 0, $bitmap.Width, $bitmap.Height)
            $bitmap.Save((Join-Path $destination $name), $jpeg, $parameters)
        } finally { $graphics.Dispose(); $bitmap.Dispose() }
    } finally { $source.Dispose() }
    $manifest.scenarios += @{ file = $name; source = $file.Name }
}
$index = 0
foreach ($file in Get-ChildItem -LiteralPath (Join-Path $workspace 'テスト用PCasset') -Filter '*.png' | Sort-Object Name) {
    $index++
    $name = 'pc-{0:d2}.png' -f $index
    Copy-Item -LiteralPath $file.FullName -Destination (Join-Path $destination $name)
    $manifest.pcs += @{ file = $name; source = $file.Name }
}
$utf8 = New-Object System.Text.UTF8Encoding($false)
[IO.File]::WriteAllText((Join-Path $destination 'sources.json'), ($manifest | ConvertTo-Json -Depth 5), $utf8)
New-Item -ItemType Directory -Force -Path (Join-Path $workspace 'frontend/src/demo') | Out-Null
Copy-Item -LiteralPath (Join-Path $workspace 'backend/src/main/resources/config/profiles.json') -Destination (Join-Path $workspace 'frontend/src/demo/profiles.json')
$parameters.Dispose()
Write-Output ('Prepared {0} scenario trailers and {1} PC portraits.' -f $manifest.scenarios.Count, $manifest.pcs.Count)
