param([switch]$NoBrowser)
. "$PSScriptRoot/env.ps1"
Set-Location $Workspace
$AppUrl='http://localhost:5173/'
$LauncherMutex=$null
$LockHeld=$false
function Test-WorkspaceApp {
 if(!(Test-Path "$Workspace/.runtime/processes.json")){return $false}
 try {
  $Recorded=Get-Content "$Workspace/.runtime/processes.json" -Raw -Encoding utf8|ConvertFrom-Json
  $Expected=@(@{id=$Recorded.backend;path="$Workspace/backend/target/takukairo-0.1.0.jar"},@{id=$Recorded.frontend;path="$Workspace/frontend/node_modules/vite/bin/vite.js"})
  foreach($App in $Expected){
   if(!$App.id){return $false}
   $AppProcess=Get-CimInstance Win32_Process -Filter "ProcessId=$($App.id)" -ErrorAction SilentlyContinue
   if(!$AppProcess -or !$AppProcess.CommandLine){return $false}
   $Command=$AppProcess.CommandLine.Replace('\','/')
   if($Command.IndexOf($App.path.Replace('\','/'),[StringComparison]::OrdinalIgnoreCase) -lt 0){return $false}
   if($Command -match '--spring.profiles.active=dev,test'){throw '検査用のアプリが起動しています。停止.cmdで停止してから、起動.cmdを開いてください。'}
  }
  return $true
 }catch {if($_.Exception.Message -like '検査用*'){throw};return $false}
}
function Test-AppReady {
 try {
  $Session=Invoke-RestMethod ($AppUrl+'api/session') -TimeoutSec 2
  return ($Session.PSObject.Properties.Name -contains 'authenticated')
 }catch{return $false}
}
try {
 $Hasher=[System.Security.Cryptography.SHA256]::Create()
 try{$WorkspaceKey=([BitConverter]::ToString($Hasher.ComputeHash([Text.Encoding]::UTF8.GetBytes($Workspace.ToLowerInvariant())))).Replace('-','')}finally{$Hasher.Dispose()}
 $LauncherMutex=New-Object System.Threading.Mutex($false,"Local\Takukairo-$WorkspaceKey")
 try{$LockHeld=$LauncherMutex.WaitOne(0)}catch [System.Threading.AbandonedMutexException]{$LockHeld=$true}
 if(!$LockHeld){Write-Host '卓回廊を起動中です。先に開いた起動ウィンドウの完了をお待ちください。';exit 0}
 $OwnApp=Test-WorkspaceApp
 $Ports=@(Get-NetTCPConnection -State Listen -LocalPort 8080,5173 -ErrorAction SilentlyContinue)
 if(!$OwnApp -and $Ports.Count){throw '起動に必要なポートが使用中です。既存のアプリを確認してください。他のプロセスは停止していません。'}
 if(!$OwnApp){
  Write-Host '卓回廊を起動しています。初回は準備に数分かかる場合があります。'
  New-Item -ItemType Directory "$Workspace/.runtime" -Force|Out-Null
  $StartupArgs=@('-NoProfile','-ExecutionPolicy','Bypass','-File',('"'+$PSScriptRoot+'/start.ps1"'))
  if(Test-Path "$Workspace/.tools/postgres/pgsql/bin/pg_ctl.exe"){$StartupArgs+='-PortablePostgres'}
  $Startup=Start-Process powershell.exe -ArgumentList $StartupArgs -WorkingDirectory $Workspace -WindowStyle Hidden -RedirectStandardOutput "$Workspace/.runtime/launcher-start.log" -RedirectStandardError "$Workspace/.runtime/launcher-start-error.log" -PassThru
  # Keep the handle open so Windows PowerShell 5.1 reliably exposes ExitCode.
  $StartupHandle=$Startup.Handle
  while(!$Startup.HasExited){Start-Sleep -Milliseconds 500;$Startup.Refresh()}
  $Startup.WaitForExit()
  if($Startup.ExitCode -ne 0){throw '起動に失敗しました。.runtime/launcher-start.log と launcher-start-error.log を確認してください。'}
 }
 $Deadline=(Get-Date).AddSeconds(60)
 $Ready=$false
 while((Get-Date) -lt $Deadline){if((Test-WorkspaceApp) -and (Test-AppReady)){$Ready=$true;break};Start-Sleep -Milliseconds 500}
 if(!$Ready){throw '接続を確認できません。停止.cmdで停止後、もう一度起動してください。保存したDataは削除されません。'}
 Write-Host '卓回廊を開けます。'
 Write-Host $AppUrl
 if(!$NoBrowser){Start-Process $AppUrl}
}catch {
 Write-Host ('起動できませんでした：'+$_.Exception.Message) -ForegroundColor Red
 exit 1
}finally {
 if($LauncherMutex){if($LockHeld){$LauncherMutex.ReleaseMutex()};$LauncherMutex.Dispose()}
}
