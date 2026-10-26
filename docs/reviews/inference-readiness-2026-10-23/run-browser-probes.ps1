$ErrorActionPreference='Stop'
$ReviewRoot='C:/Src/takukairo'
$ReviewDir="$ReviewRoot/.runtime/inference-review-2026-10-23"
$ReviewPg="$ReviewRoot/.tools/postgres/pgsql/bin"
$ReviewJava="$ReviewRoot/.tools/java/jdk-25.0.4.1+1/bin/java.exe"
$ReviewNode="$ReviewRoot/.tools/node/node-v24.21.0-win-x64/node.exe"
$ReviewBackend=$null;$ReviewFrontend=$null;$ReviewPostgres=$null
foreach($ReviewPort in @(55433,8083,5176)){$ReviewListener=[Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback,$ReviewPort);try{$ReviewListener.Start()}finally{$ReviewListener.Stop()}}
try{
 if(!(Test-Path -LiteralPath "$ReviewDir/pgdata/PG_VERSION")){& "$ReviewPg/initdb.exe" -D "$ReviewDir/pgdata" -U review_qa --auth=trust --encoding=UTF8 --locale=C;if($LASTEXITCODE){throw 'initdb failed'}}
 $ReviewPostgres=Start-Process "$ReviewPg/postgres.exe" -ArgumentList '-D',"$ReviewDir/pgdata",'-p','55433','-h','127.0.0.1' -WindowStyle Hidden -PassThru -RedirectStandardOutput "$ReviewDir/postgres.log" -RedirectStandardError "$ReviewDir/postgres-error.log"
 $ReviewDeadline=(Get-Date).AddSeconds(30);do{& "$ReviewPg/pg_isready.exe" -h 127.0.0.1 -p 55433|Out-Null;if(!$LASTEXITCODE){break};Start-Sleep -Milliseconds 250}while((Get-Date)-lt $ReviewDeadline)
 if($LASTEXITCODE){throw 'Postgres not ready'}
 $ReviewExists=& "$ReviewPg/psql.exe" -h 127.0.0.1 -p 55433 -U review_qa -d postgres -Atc "select 1 from pg_database where datname='takukairo_review_20261023'"
 if(!$ReviewExists){& "$ReviewPg/createdb.exe" -h 127.0.0.1 -p 55433 -U review_qa takukairo_review_20261023;if($LASTEXITCODE){throw 'createdb failed'}}
 $ReviewBackend=Start-Process $ReviewJava -ArgumentList '--enable-native-access=ALL-UNNAMED','-jar',"$ReviewRoot/.runtime/import-inference-qa/backend-build/takukairo-0.1.0.jar",'--spring.profiles.active=dev,test','--server.port=8083','--spring.datasource.url=jdbc:postgresql://127.0.0.1:55433/takukairo_review_20261023','--spring.datasource.username=review_qa','--spring.datasource.password=',"--app.storage=$ReviewDir/storage" -WindowStyle Hidden -PassThru -RedirectStandardOutput "$ReviewDir/backend.log" -RedirectStandardError "$ReviewDir/backend-error.log"
 $env:TAKUKAIRO_BACKEND_ORIGIN='http://127.0.0.1:8083'
 try{$ReviewFrontend=Start-Process $ReviewNode -ArgumentList 'node_modules/vite/bin/vite.js','--port','5176','--host','localhost' -WorkingDirectory "$ReviewRoot/frontend" -WindowStyle Hidden -PassThru -RedirectStandardOutput "$ReviewDir/frontend.log" -RedirectStandardError "$ReviewDir/frontend-error.log"}finally{Remove-Item Env:TAKUKAIRO_BACKEND_ORIGIN}
 $ReviewDeadline=(Get-Date).AddSeconds(60);$ReviewReady=$false
 do{try{$ReviewResponse=Invoke-WebRequest 'http://localhost:5176/api/session' -UseBasicParsing -TimeoutSec 2;if($ReviewResponse.StatusCode-eq 200){$ReviewReady=$true;break}}catch{};Start-Sleep -Milliseconds 250}while((Get-Date)-lt $ReviewDeadline)
 if(!$ReviewReady){throw 'App not ready'}
 & $ReviewNode "$ReviewDir/browser-probe.mjs";if($LASTEXITCODE){throw 'Browser probe failed'}
}finally{
 foreach($ReviewProcess in @($ReviewFrontend,$ReviewBackend)){if($ReviewProcess-and !$ReviewProcess.HasExited){Stop-Process -Id $ReviewProcess.Id}}
 if($ReviewPostgres-and !$ReviewPostgres.HasExited){& "$ReviewPg/pg_ctl.exe" -D "$ReviewDir/pgdata" -m fast -w stop|Out-Null}
}
