. "$PSScriptRoot/env.ps1"
if(Test-Path "$Workspace/.runtime/processes.json"){
 $Processes=Get-Content "$Workspace/.runtime/processes.json" -Raw -Encoding utf8|ConvertFrom-Json
 foreach($TaskProcessId in @($Processes.backend,$Processes.frontend)){
  if(!$TaskProcessId){continue}
  $TaskProcess=Get-CimInstance Win32_Process -Filter "ProcessId=$TaskProcessId" -ErrorAction SilentlyContinue
  if(!$TaskProcess){continue}
  $AppCommand=$TaskProcess.CommandLine
  $KnownApp=$AppCommand -and (($AppCommand -like '*target/takukairo-0.1.0.jar*') -or ($AppCommand -like '*target\takukairo-0.1.0.jar*') -or ($AppCommand -like '*node_modules/vite/bin/vite.js*'))
  $WorkspaceExecutable=$TaskProcess.ExecutablePath -and $TaskProcess.ExecutablePath.StartsWith($Workspace,[StringComparison]::OrdinalIgnoreCase)
  if($KnownApp -and ($WorkspaceExecutable -or $AppCommand.Contains($Workspace))){Stop-Process -Id $TaskProcessId}else{Write-Warning "Recorded process $TaskProcessId is not the expected local app; preserved."}
 }
 if($Processes.portablePostgres){& "$Workspace/.tools/postgres/pgsql/bin/pg_ctl.exe" -D "$Workspace/.runtime/pgdata" status *> $null;if(!$LASTEXITCODE){& "$Workspace/.tools/postgres/pgsql/bin/pg_ctl.exe" -D "$Workspace/.runtime/pgdata" stop -m fast;if($LASTEXITCODE){throw 'PostgreSQL stop failed'}}}else{Push-Location $Workspace;try{docker compose stop db;if($LASTEXITCODE){throw 'Docker stop failed'}}finally{Pop-Location}}
}
