@echo off
title Takukairo Local
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\close-local.ps1"
if errorlevel 1 pause
