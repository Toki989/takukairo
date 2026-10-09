$ErrorActionPreference='Stop'
$Workspace=Split-Path $PSScriptRoot -Parent
$JavaDir=Join-Path $Workspace '.tools/java/jdk-25.0.4.1+1'
$NodeDir=Join-Path $Workspace '.tools/node/node-v24.21.0-win-x64'
$MavenDir=Join-Path $Workspace '.tools/maven/apache-maven-3.9.16/bin'
if(Test-Path "$JavaDir/bin/java.exe"){$env:JAVA_HOME=$JavaDir;$env:PATH="$JavaDir/bin;$env:PATH"}
if(Test-Path "$NodeDir/node.exe"){$env:PATH="$NodeDir;$env:PATH"}
if(Test-Path "$MavenDir/mvn.cmd"){$env:PATH="$MavenDir;$env:PATH"}
$DockerDir=Join-Path $env:LOCALAPPDATA 'Programs/DockerDesktop/resources/bin'
if(Test-Path "$DockerDir/docker.exe"){$env:PATH="$DockerDir;$env:PATH"}
if(Test-Path "$Workspace/.env") {foreach($Line in Get-Content "$Workspace/.env" -Encoding utf8){if($Line -match '^([A-Z_]+)=(.*)$'){[Environment]::SetEnvironmentVariable($Matches[1],$Matches[2],'Process')}}}
$env:MAVEN_OPTS='-Dfile.encoding=UTF-8'
