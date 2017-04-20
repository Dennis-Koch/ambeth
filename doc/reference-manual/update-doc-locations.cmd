@echo off
@SETLOCAL ENABLEDELAYEDEXPANSION
@set /p VERSION=<target\version.txt
@set INITIAL_FILE_NAME=Ambeth-Reference.pdf
@set FILE_NAME=Ambeth-Reference-%VERSION%.pdf
rem @set FILE_DIR=%~dp0target\site
@set FILE_DIR=%~dp0
@set INITIAL_FILE_PATH=%FILE_DIR%\%INITIAL_FILE_NAME%
@set FILE_PATH=%FILE_DIR%\%FILE_NAME%
@set MY_GIT_DIR=%~dp0..\..\..\ambeth.wiki
@set HORIZON_DRIVE=\\svetfile05\RHE.01.R-D.Software.pub\Horizon
copy %INITIAL_FILE_PATH% %FILE_PATH%
cd /D %MY_GIT_DIR%
echo.
echo **********************************************
echo Copying reference manual v%VERSION% to Ambeth Wiki GIT...
echo **********************************************
git filter-branch --tree-filter "rm -rf ./reference-manual/%VERSION%" -f --prune-empty HEAD
rem git for-each-ref --format="%(refname)" refs/original/ | xargs -n 1 git update-ref -d
git gc
mkdir reference-manual\%VERSION%
copy %FILE_PATH% reference-manual\%VERSION%
git add reference-manual/%VERSION%
git commit -m "updated reference-manual v%VERSION%"
git push origin master --force
echo.
echo **********************************************
echo Copying reference manual v%VERSION% to %HORIZON_DRIVE%...
echo **********************************************
copy %FILE_PATH% %HORIZON_DRIVE%
ENDLOCAL