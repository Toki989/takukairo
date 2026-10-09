param([switch]$PortablePostgres,[switch]$Qa)
. "$PSScriptRoot/env.ps1"
Set-Location $Workspace
New-Item -ItemType Directory "$Workspace/.runtime" -Force | Out-Null
foreach($Port in @(8080,5173)){if(Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue){throw "Port $Port is already in use. Run scripts/stop.ps1 for the previous local app first."}}
if($PortablePostgres){
 $PgBin=Join-Path $Workspace '.tools/postgres/pgsql/bin'
 $PgData=Join-Path $Workspace '.runtime/pgdata'
 if(!(Test-Path "$PgData/PG_VERSION")){& "$PgBin/initdb.exe" -D $PgData -U takukairo -A trust --encoding=UTF8 --locale=C;if($LASTEXITCODE){throw 'initdb failed'};Add-Content "$PgData/postgresql.conf" "listen_addresses='127.0.0.1'"}
 & "$PgBin/pg_ctl.exe" -D $PgData status *> $null
 if($LASTEXITCODE){& "$PgBin/pg_ctl.exe" -D $PgData -l "$Workspace/.runtime/postgres.log" start;if($LASTEXITCODE){throw 'PostgreSQL start failed'}}
 $Exists=& "$PgBin/psql.exe" -h localhost -U takukairo -d postgres -At -c "select count(*) from pg_database where datname='takukairo'"
 if($Exists -eq '0'){& "$PgBin/createdb.exe" -h localhost -U takukairo takukairo}
}else{docker compose up -d --wait db;if($LASTEXITCODE){throw 'Docker PostgreSQL failed'}}
& mvn.cmd -B -ntp -f backend/pom.xml "-Dmaven.repo.local=$Workspace/.tools/m2" -DskipTests package
if($LASTEXITCODE){throw 'Backend build failed'}
Push-Location "$Workspace/frontend"
try{& npm.cmd ci; if($LASTEXITCODE){throw 'npm ci failed'}}finally{Pop-Location}
$AppProfiles=if($Qa){'dev,test'}else{'dev'}
$Backend=Start-Process -FilePath "$env:JAVA_HOME/bin/java.exe" -ArgumentList '--enable-native-access=ALL-UNNAMED','-jar',('"'+$Workspace+'/backend/target/takukairo-0.1.0.jar"'),"--spring.profiles.active=$AppProfiles" -WorkingDirectory "$Workspace/backend" -WindowStyle Hidden -RedirectStandardOutput "$Workspace/.runtime/backend.log" -RedirectStandardError "$Workspace/.runtime/backend-error.log" -PassThru
$NodePath=(Get-Command node.exe).Source
$Frontend=Start-Process -FilePath $NodePath -ArgumentList ('"'+$Workspace+'/frontend/node_modules/vite/bin/vite.js"') -WorkingDirectory "$Workspace/frontend" -WindowStyle Hidden -RedirectStandardOutput "$Workspace/.runtime/frontend.log" -RedirectStandardError "$Workspace/.runtime/frontend-error.log" -PassThru
@{backend=$Backend.Id;frontend=$Frontend.Id;portablePostgres=[bool]$PortablePostgres}|ConvertTo-Json|Set-Content "$Workspace/.runtime/processes.json" -Encoding utf8
$Deadline=(Get-Date).AddSeconds(60)
$Ready=$false
while((Get-Date) -lt $Deadline){
 if($Backend.HasExited -or $Frontend.HasExited){throw 'Local app exited. Check .runtime/backend.log and frontend.log.'}
 try{$Response=Invoke-WebRequest 'http://localhost:5173/api/session' -UseBasicParsing -TimeoutSec 2;if($Response.StatusCode -eq 200){$Ready=$true;break}}catch{Start-Sleep -Milliseconds 500}
}
if(!$Ready){throw 'Startup timed out. Processes are recorded for scripts/stop.ps1; check logs.'}
Write-Host 'Frontend: http://localhost:5173/dev-login'
Write-Host 'Logs: .runtime/backend.log, .runtime/frontend.log'
