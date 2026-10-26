param([string]$RunName='run1')
$ErrorActionPreference='Stop'
$repo= [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
if($RunName -notmatch '^run[0-9]+$'){throw 'RunName must be run followed by digits'}
$run=Join-Path $repo ".runtime/round1-acceptance-2026-10-26/$RunName"
$pg=Join-Path $repo '.tools/postgres/pgsql/bin'
$java=Join-Path $repo '.tools/java/jdk-25.0.4.1+1'
$node=Join-Path $repo '.tools/node/node-v24.21.0-win-x64/node.exe'
$qa=Join-Path $repo '.runtime/import-inference-qa'
$db="takukairo_round1_20261026_$RunName"
foreach($port in @(55432,8082,5175)){
 foreach($address in @([Net.IPAddress]::Loopback,[Net.IPAddress]::IPv6Loopback)){
  $probe=[Net.Sockets.TcpListener]::new($address,$port)
  try{$probe.Start()}finally{$probe.Stop()}
 }
}
New-Item -ItemType Directory -Force -Path "$run/input","$run/expected","$run/screenshots" | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive='C:\Users\academia\Downloads\takukairo_import_round1 (2).zip'
$zip=[IO.Compression.ZipFile]::OpenRead($archive)
try{
 foreach($entry in $zip.Entries){
  if($entry.FullName -match '/(input/[^/]+|expected/expected_results.json)$'){
   $relative=$Matches[1]
   [IO.Compression.ZipFileExtensions]::ExtractToFile($entry,(Join-Path $run $relative),$true)
  }
 }
}finally{$zip.Dispose()}
$env:JAVA_HOME=$java
$env:PATH="$java/bin;$env:PATH"
$env:MAVEN_OPTS='-Dfile.encoding=UTF-8'
& "$repo/.tools/maven/apache-maven-3.9.16/bin/mvn.cmd" -B -ntp -Pimport-qa -f "$repo/backend/pom.xml" "-Dmaven.repo.local=$repo/.tools/m2" -DskipTests package *> "$run/build.log"
if($LASTEXITCODE){throw "Build failed; see $run/build.log"}
$postgres=$null;$backend=$null;$frontend=$null
try{
 $postgres=Start-Process "$pg/postgres.exe" -ArgumentList '-D',"$qa/pgdata",'-p','55432','-h','127.0.0.1' -WindowStyle Hidden -PassThru -RedirectStandardOutput "$run/postgres.log" -RedirectStandardError "$run/postgres-error.log"
 $deadline=(Get-Date).AddSeconds(30)
 do {& "$pg/pg_isready.exe" -h 127.0.0.1 -p 55432 | Out-Null;if(!$LASTEXITCODE){break};Start-Sleep -Milliseconds 250}while((Get-Date) -lt $deadline)
 if($LASTEXITCODE){throw 'Dedicated PostgreSQL did not start'}
 $exists=& "$pg/psql.exe" -h 127.0.0.1 -p 55432 -U import_qa -d postgres -Atc "select 1 from pg_database where datname='$db'"
 if($exists){throw "Database $db already exists; select a new RunName to keep the test isolated"}
 & "$pg/createdb.exe" -h 127.0.0.1 -p 55432 -U import_qa $db
 if($LASTEXITCODE){throw 'Database creation failed'}
 $backend=Start-Process "$java/bin/java.exe" -ArgumentList '--enable-native-access=ALL-UNNAMED','-jar',"$qa/backend-build/takukairo-0.1.0.jar",'--spring.profiles.active=dev,test','--spring.flyway.locations=classpath:db/migration','--server.port=8082',"--spring.datasource.url=jdbc:postgresql://127.0.0.1:55432/$db",'--spring.datasource.username=import_qa','--spring.datasource.password=',"--app.storage=$run/storage" -WindowStyle Hidden -PassThru -RedirectStandardOutput "$run/backend.log" -RedirectStandardError "$run/backend-error.log"
 $env:TAKUKAIRO_BACKEND_ORIGIN='http://127.0.0.1:8082'
 try{$frontend=Start-Process $node -ArgumentList 'node_modules/vite/bin/vite.js','--port','5175','--host','localhost' -WorkingDirectory "$repo/frontend" -WindowStyle Hidden -PassThru -RedirectStandardOutput "$run/frontend.log" -RedirectStandardError "$run/frontend-error.log"}finally{Remove-Item Env:TAKUKAIRO_BACKEND_ORIGIN}
 $deadline=(Get-Date).AddSeconds(60);$ready=$false
 do{try{$response=Invoke-WebRequest 'http://localhost:5175/api/session' -UseBasicParsing -TimeoutSec 2;if($response.StatusCode -eq 200){$ready=$true;break}}catch{};Start-Sleep -Milliseconds 250}while((Get-Date) -lt $deadline)
 if(!$ready){throw 'Dedicated app did not start'}
 @{run=$run;database=$db;frontendPort=5175;backendPort=8082;postgresPort=55432;backendPid=$backend.Id;frontendPid=$frontend.Id;archiveSha256=(Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash} | ConvertTo-Json | Set-Content -Encoding UTF8 -LiteralPath "$run/environment.json"
 Write-Output "READY: $run (database $db). Create $run/stop to stop only this run's processes."
 $deadline=(Get-Date).AddMinutes(45)
 while(!(Test-Path -LiteralPath "$run/stop") -and (Get-Date) -lt $deadline){Start-Sleep -Seconds 1}
}finally{
 foreach($owned in @($frontend,$backend)){if($owned -and !$owned.HasExited){Stop-Process -Id $owned.Id}}
 if($postgres -and !$postgres.HasExited){& "$pg/pg_ctl.exe" -D "$qa/pgdata" -m fast -w stop | Out-Null}
 Write-Output 'Dedicated services stopped. Test data preserved; normal app untouched.'
}
