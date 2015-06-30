@echo off

SETLOCAL
IF NOT DEFINED PROJECT_HOME ( set PROJECT_HOME=%WORKSPACE%)

rem Define all needed variables
set baseDir=%PROJECT_HOME%\doc\reference-manual\target
set srcDir=%baseDir%\src

set srcHome=%PROJECT_HOME%
set javaSrcDir=%srcDir%\java
set javaLibDir=%javaSrcDir%\libs
set javaModuleDir=%srcHome%\osthus-ambeth\jambeth
set javaModuleDir2=%srcHome%\ambeth
set integrityDir=%srcHome%\integrity

set csSrcDir=%srcDir%\cs
set csLibDir=%csSrcDir%\libs
set csModuleDir=%srcHome%\osthus-ambeth\ambeth
set csAmbethProperties=%csSrcDir%\ambeth.properties
set csSkipModuleScan=false

set resultType=tc
set dataDir=%baseDir%\data


rem If desired removed the target dir to start clean
rmdir /s /q "%baseDir%" 1> nul 2> nul

rem Create all needed folders if neccesary
mkdir "%baseDir%" 1> nul 2> nul
mkdir "%srcDir%" 1> nul 2> nul

mkdir "%javaSrcDir%" 1> nul 2> nul
mkdir "%javaLibDir%" 1> nul 2> nul

mkdir "%csSrcDir%" 1> nul 2> nul
mkdir "%csLibDir%" 1> nul 2> nul

mkdir "%dataDir%" 1> nul 2> nul

rem Copy all jAmbeth jars
call D:\jenkins\tools\hudson.tasks.Maven_MavenInstallation\Maven3\bin\mvn.bat  -f "%javaModuleDir2%\pom.xml" dependency:copy-dependencies -DoutputDirectory="%javaSrcDir%" -DincludeGroupIds=de.osthus.ambeth

rem Copy all external library jars
call D:\jenkins\tools\hudson.tasks.Maven_MavenInstallation\Maven3\bin\mvn.bat  -f "%javaModuleDir2%\pom.xml" dependency:copy-dependencies -DoutputDirectory="%javaLibDir%" -DexcludeGroupIds=de.osthus.ambeth

rem Copy all jAmbeth jars
call D:\jenkins\tools\hudson.tasks.Maven_MavenInstallation\Maven3\bin\mvn.bat  -f "%javaModuleDir%\pom.xml" dependency:copy-dependencies -DoutputDirectory="%javaSrcDir%" -DincludeGroupIds=de.osthus.ambeth

rem Copy all external library jars
call D:\jenkins\tools\hudson.tasks.Maven_MavenInstallation\Maven3\bin\mvn.bat  -f "%javaModuleDir%\pom.xml" dependency:copy-dependencies -DoutputDirectory="%javaLibDir%" -DexcludeGroupIds=de.osthus.ambeth

rem Copy all C# libs
for /r "%csModuleDir%" %%x in (Ambeth.*.dll Ambeth.*.pdb Minerva.*.dll Minerva.*.pdb) do (
  copy "%%x" "%csSrcDir%\" > nul
)

del "%csSrcDir%\*.SL*.dll"
del "%csSrcDir%\*.SL*.pdb"

rem Copy all external library libs
xcopy "%csModuleDir%\Ambeth.Util\libs\*.dll" "%csLibDir%" /I /Y > nul
xcopy "%csModuleDir%\Lib.Telerik\*.dll" "%csLibDir%" /I /Y > nul

rem From Jenkins Job
set resultType=tcs
set javaModules=jambeth-audit-server,jambeth-bytecode,jambeth-cache,jambeth-cache-bytecode,jambeth-cache-datachange,jambeth-cache-server,jambeth-cache-stream,jambeth-datachange,jambeth-datachange-persistence,jambeth-event,jambeth-event-datachange,jambeth-event-server,jambeth-filter,jambeth-ioc,jambeth-job,jambeth-job-cron4j,jambeth-log,jambeth-mapping,jambeth-merge,jambeth-merge-bytecode,jambeth-merge-server,jambeth-persistence,jambeth-persistence-api,jambeth-persistence-h2,jambeth-persistence-jdbc,jambeth-persistence-oracle11,jambeth-platform,jambeth-query,jambeth-query-inmemory,jambeth-query-jdbc,jambeth-rdf,jambeth-security,jambeth-security-bytecode,jambeth-security-server,jambeth-sensor,jambeth-server-rest,jambeth-service,jambeth-stream,jambeth-testutil,jambeth-testutil-persistence,jambeth-util,jambeth-xml
set csModules=Ambeth.Bytecode,Ambeth.Cache,Ambeth.Cache.Bytecode,Ambeth.CacheDataChange,Ambeth.DataChange,Ambeth.Event,Ambeth.Event.DataChange,Ambeth.Filter,Ambeth.IoC,Ambeth.Log,Ambeth.Mapping,Ambeth.Merge,Ambeth.Merge.Bytecode,Ambeth.Privilege,Ambeth.Security,Ambeth.Service,Ambeth.TestUtil,Ambeth.Util,Ambeth.Xml

rem @call mvn exec:java -Dexec.mainClass="de.osthus.classbrowser.java.Program" -DjarFolders="%javaSrcDir%" -DlibraryJarFolders="%javaLibDir%" -DtargetPath="%dataDir%" -DmoduleRootPath="%javaModuleDir%"
@call D:\jenkins\tools\hudson.tasks.Maven_MavenInstallation\Maven3\bin\mvn.bat  exec:java -Dexec.mainClass="de.osthus.classbrowser.java.Program" -DjarFolders="%javaSrcDir%" -DlibraryJarFolders="%javaLibDir%" -DtargetPath="%dataDir%" -DmoduleRootPath="%javaModuleDir%"

rem Create xml containing the description of the C# code
set csClassBrowserDir="%integrityDir%\de.osthus.classbrowser.csharp\CsharpClassbrowser"

@C:\Windows\Microsoft.NET\Framework\v4.0.30319\MSBuild.exe "%csClassBrowserDir%\CsharpClassbrowser.sln" "/p:ContinueOnError=false" "/p:StopOnFirstFailure=true"
@"%csClassBrowserDir%\CsharpClassbrowser\bin\Debug\CsharpClassbrowser.exe" -assemblyPaths="%csSrcDir%" -libraryAssemblyPaths="%csLibDir%" -targetPath="%dataDir%" -moduleRootPath="%csModuleDir%"

:end
pause