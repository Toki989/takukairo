# Dedicated PostgreSQL 18.6 cluster, integration DB and browser DB. Never uses normal app data.
param([string]$BackendTests='', [switch]$BackendOnly, [switch]$MeasureScale)
$ErrorActionPreference='Stop'
$ImportWorkspace=Split-Path $PSScriptRoot -Parent
$ImportQa=Join-Path $ImportWorkspace '.runtime/import-inference-qa'
$ImportJava=Join-Path $ImportWorkspace '.tools/java/jdk-25.0.4.1+1'
$ImportNode=Join-Path $ImportWorkspace '.tools/node/node-v24.21.0-win-x64/node.exe'
$ImportPg=Join-Path $ImportWorkspace '.tools/postgres/pgsql/bin'
$ImportMaven=Join-Path $ImportWorkspace '.tools/maven/apache-maven-3.9.16/bin/mvn.cmd'
$env:JAVA_HOME=$ImportJava
$env:PATH="$ImportJava/bin;$env:PATH"
$env:MAVEN_OPTS='-Dfile.encoding=UTF-8'
foreach($ImportPort in @(55432,8082,5175)){foreach($ImportAddress in @([System.Net.IPAddress]::Loopback,[System.Net.IPAddress]::IPv6Loopback)){$ImportProbe=[System.Net.Sockets.TcpListener]::new($ImportAddress,$ImportPort);try{$ImportProbe.Start()}catch{throw "Dedicated QA port $ImportPort is unavailable. Stop its owner before running this suite."}finally{$ImportProbe.Stop()}}}
New-Item -ItemType Directory -Force -Path $ImportQa | Out-Null
$ImportPgProcess=$null;$ImportBackend=$null;$ImportFrontend=$null
try {
 if(!(Test-Path -LiteralPath "$ImportQa/pgdata/PG_VERSION")){& "$ImportPg/initdb.exe" -D "$ImportQa/pgdata" -U import_qa --auth=trust --encoding=UTF8 --locale=C;if($LASTEXITCODE){throw 'Dedicated initdb failed'}}
 $ImportPgProcess=Start-Process "$ImportPg/postgres.exe" -ArgumentList '-D',"$ImportQa/pgdata",'-p','55432','-h','127.0.0.1' -WindowStyle Hidden -PassThru -RedirectStandardOutput "$ImportQa/postgres.log" -RedirectStandardError "$ImportQa/postgres-error.log"
 $ImportDeadline=(Get-Date).AddSeconds(30)
 do {& "$ImportPg/pg_isready.exe" -h 127.0.0.1 -p 55432 | Out-Null;if(!$LASTEXITCODE){break};Start-Sleep -Milliseconds 250} while((Get-Date) -lt $ImportDeadline)
 if($LASTEXITCODE){throw 'Dedicated PostgreSQL startup failed'}
 foreach($ImportDb in @('takukairo_import_qa','takukairo_import_browser_qa')){
  $ImportExists=& "$ImportPg/psql.exe" -h 127.0.0.1 -p 55432 -U import_qa -d postgres -Atc "select 1 from pg_database where datname='$ImportDb'"
  if($LASTEXITCODE){throw 'Dedicated database lookup failed'}
  if(!$ImportExists){& "$ImportPg/createdb.exe" -h 127.0.0.1 -p 55432 -U import_qa $ImportDb;if($LASTEXITCODE){throw 'Dedicated database creation failed'}}
 }
 $ImportTestArgs=@('-B','-ntp','-Pimport-qa','-f',"$ImportWorkspace/backend/pom.xml","-Dmaven.repo.local=$ImportWorkspace/.tools/m2",'-Dtakukairo.import-test-db=true')
 if($BackendTests){$ImportTestArgs+="-Dtest=$BackendTests"}
 & $ImportMaven @ImportTestArgs test
 if($LASTEXITCODE){throw 'Backend tests failed'}
 if($BackendOnly){return}
 & $ImportMaven -B -ntp -Pimport-qa -f "$ImportWorkspace/backend/pom.xml" "-Dmaven.repo.local=$ImportWorkspace/.tools/m2" -DskipTests package
 if($LASTEXITCODE){throw 'Backend package failed'}
 Push-Location "$ImportWorkspace/frontend"
 try {
  & $ImportNode node_modules/typescript/bin/tsc --noEmit;if($LASTEXITCODE){throw 'Frontend typecheck failed'}
  & $ImportNode node_modules/typescript/bin/tsc -b;if($LASTEXITCODE){throw 'Frontend project build failed'}
  & $ImportNode node_modules/vite/bin/vite.js build;if($LASTEXITCODE){throw 'Frontend bundle failed'}
  & $ImportNode --experimental-strip-types --test qa/import-state.test.mjs;if($LASTEXITCODE){throw 'Frontend state tests failed'}
  $ImportBackend=Start-Process "$ImportJava/bin/java.exe" -ArgumentList '--enable-native-access=ALL-UNNAMED','-jar',"$ImportQa/backend-build/takukairo-0.1.0.jar",'--spring.profiles.active=dev,test','--server.port=8082','--spring.datasource.url=jdbc:postgresql://127.0.0.1:55432/takukairo_import_browser_qa','--spring.datasource.username=import_qa','--spring.datasource.password=',"--app.storage=$ImportQa/browser-storage" -WindowStyle Hidden -PassThru -RedirectStandardOutput "$ImportQa/backend.log" -RedirectStandardError "$ImportQa/backend-error.log"
  $env:TAKUKAIRO_BACKEND_ORIGIN='http://127.0.0.1:8082'
  $ImportFrontend=Start-Process $ImportNode -ArgumentList 'node_modules/vite/bin/vite.js','--port','5175','--host','localhost' -WorkingDirectory "$ImportWorkspace/frontend" -WindowStyle Hidden -PassThru -RedirectStandardOutput "$ImportQa/frontend.log" -RedirectStandardError "$ImportQa/frontend-error.log"
  Remove-Item Env:TAKUKAIRO_BACKEND_ORIGIN
  $ImportDeadline=(Get-Date).AddSeconds(60);$ImportReady=$false
  do {try{$ImportResponse=Invoke-WebRequest 'http://localhost:5175/api/session' -UseBasicParsing -TimeoutSec 2;if($ImportResponse.StatusCode -eq 200){$ImportReady=$true;break}}catch{};Start-Sleep -Milliseconds 250} while((Get-Date) -lt $ImportDeadline)
  if(!$ImportReady){throw 'Dedicated app startup failed'}
  & $ImportNode node_modules/@playwright/test/cli.js test --config playwright.import.config.ts
  if($LASTEXITCODE){throw 'Import browser tests failed'}
  if($MeasureScale){
   $env:TAKUKAIRO_IMPORT_SCALE_ORIGIN='http://127.0.0.1:8082';$env:TAKUKAIRO_IMPORT_QA_PID=[string]$ImportBackend.Id
   try {& $ImportNode "$ImportWorkspace/scripts/measure-import-inference.mjs";if($LASTEXITCODE){throw 'Import scalability measurement failed'}}
   finally {Remove-Item Env:TAKUKAIRO_IMPORT_SCALE_ORIGIN;Remove-Item Env:TAKUKAIRO_IMPORT_QA_PID}
  }
 } finally {Pop-Location}
} finally {
 foreach($ImportProcess in @($ImportFrontend,$ImportBackend)){if($ImportProcess -and !$ImportProcess.HasExited){Stop-Process -Id $ImportProcess.Id}}
 if($ImportPgProcess -and !$ImportPgProcess.HasExited){& "$ImportPg/pg_ctl.exe" -D "$ImportQa/pgdata" -m fast -w stop | Out-Null}
 Write-Host "Dedicated QA data preserved in $ImportQa. Normal database and images unchanged."
}

# Only reached when the test stages and cleanup return normally.
exit 0
