. "$PSScriptRoot/env.ps1"
try {
 & "$PSScriptRoot/stop.ps1"
 Write-Host '卓回廊を停止しました。保存した記録・画像・途中保存は保持しています。'
}catch {
 Write-Host ('停止を完了できませんでした：'+$_.Exception.Message) -ForegroundColor Red
 exit 1
}
