param([switch]$KeepRunning)
. "$PSScriptRoot/env.ps1"
Set-Location $Workspace
$QaDbName='takukairo_qa_'+(Get-Date -Format 'yyyyMMddHHmmss')
$PgBin=if(Test-Path "$Workspace/.tools/postgres/pgsql/bin/createdb.exe"){"$Workspace/.tools/postgres/pgsql/bin"}else{Split-Path (Get-Command createdb.exe).Source}
$env:PGPASSWORD=if($env:DB_PASSWORD){$env:DB_PASSWORD}else{'local-test-only'}
$DbUser=if($env:DB_USER){$env:DB_USER}else{'takukairo'}
foreach($Port in @(8081,5174)){if(Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue){throw "QA port $Port is already in use."}}
& "$PgBin/createdb.exe" -h localhost -U $DbUser $QaDbName;if($LASTEXITCODE){throw 'QA database create failed'}
$QaBackend=$null;$QaFrontend=$null
try{
 $QaBackend=Start-Process "$env:JAVA_HOME/bin/java.exe" -ArgumentList '--enable-native-access=ALL-UNNAMED','-jar',"$Workspace/backend/target/takukairo-0.1.0.jar",'--spring.profiles.active=dev,test','--server.port=8081',"--spring.datasource.url=jdbc:postgresql://localhost:5432/$QaDbName",'--spring.flyway.locations=classpath:db/migration',"--app.storage=$Workspace/.runtime/qa-state-storage" -WorkingDirectory "$Workspace/backend" -WindowStyle Hidden -RedirectStandardOutput "$Workspace/.runtime/qa-state-backend.log" -RedirectStandardError "$Workspace/.runtime/qa-state-backend-error.log" -PassThru
 $env:TAKUKAIRO_BACKEND_ORIGIN='http://127.0.0.1:8081'
 $QaFrontend=Start-Process (Get-Command node.exe).Source -ArgumentList "$Workspace/frontend/node_modules/vite/bin/vite.js",'--port','5174' -WorkingDirectory "$Workspace/frontend" -WindowStyle Hidden -RedirectStandardOutput "$Workspace/.runtime/qa-state-frontend.log" -RedirectStandardError "$Workspace/.runtime/qa-state-frontend-error.log" -PassThru
 Remove-Item Env:TAKUKAIRO_BACKEND_ORIGIN
 $Deadline=(Get-Date).AddSeconds(60);$Ready=$false
 while((Get-Date) -lt $Deadline){try{$Response=Invoke-WebRequest 'http://localhost:5174/api/session' -UseBasicParsing -TimeoutSec 2;if($Response.StatusCode -eq 200){$Ready=$true;break}}catch{Start-Sleep -Milliseconds 500}}
 if(!$Ready){throw 'QA startup timed out; check qa-state logs.'}
 Push-Location "$Workspace/frontend";try{& node.exe node_modules/@playwright/test/cli.js test --config playwright.states.config.ts;$TestCode=$LASTEXITCODE}finally{Pop-Location}
 if($TestCode){throw 'QA state browser tests failed'}
}finally{
 if(!$KeepRunning){if($QaFrontend -and !$QaFrontend.HasExited){Stop-Process $QaFrontend.Id};if($QaBackend -and !$QaBackend.HasExited){Stop-Process $QaBackend.Id}}
 Write-Host "QA Database preserved: $QaDbName (normal takukairo DB unchanged)"
}
