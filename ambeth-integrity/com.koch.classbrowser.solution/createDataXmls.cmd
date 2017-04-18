@echo off

rem Define all needed variables
set baseDir=%CD%\target
set binDir=%CD%\bin
set srcDir=%baseDir%\src

set javaSrcDir=%srcDir%\java
set javaLibDir=%javaSrcDir%\libs
set javaModuleDir=..\..\jAmbeth

set csSrcDir=%srcDir%\cs
set csLibDir=%csSrcDir%\libs
set csModuleDir=..\..\Ambeth
set csAmbethProperties=%csSrcDir%\ambeth.properties
set csSkipModuleScan=true

set resultType=tc
set dataDir=%baseDir%\data

rem Check existance of class browser binaries
if not exist %binDir% (
  echo A folder "bin" containing the files "JavaClassbrowser.jar" and "CsharpClassbrowser.exe" from the Jenkins project "Ambeth-Integrity-Tools" is needed.
  goto end
)
if not exist %binDir%\JavaClassbrowser.jar (
  echo A folder "bin" containing the files "JavaClassbrowser.jar" and "CsharpClassbrowser.exe" from the Jenkins project "Ambeth-Integrity-Tools" is needed.
  goto end
)
if not exist %binDir%\CsharpClassbrowser.exe (
  echo A folder "bin" containing the files "JavaClassbrowser.jar" and "CsharpClassbrowser.exe" from the Jenkins project "Ambeth-Integrity-Tools" is needed.
  goto end
)

rem If desired removed the target dir to start clean
if "%1" == "clean" (
  rmdir /s /q %baseDir% 1> nul 2> nul
)

rem Create all needed folders if neccesary
mkdir %baseDir% 1> nul 2> nul
mkdir %srcDir% 1> nul 2> nul

mkdir %javaSrcDir% 1> nul 2> nul
mkdir %javaLibDir% 1> nul 2> nul

mkdir %csSrcDir% 1> nul 2> nul
mkdir %csLibDir% 1> nul 2> nul

mkdir %dataDir% 1> nul 2> nul

rem Copy all jAmbeth jars
call mvn -f %javaModuleDir%\pom.xml dependency:copy-dependencies -DoutputDirectory=%javaSrcDir% -DincludeGroupIds=de.osthus.ambeth

rem Copy all external library jars
call mvn -f %javaModuleDir%\pom.xml dependency:copy-dependencies -DoutputDirectory=%javaLibDir% -DexcludeGroupIds=de.osthus.ambeth

rem Copy all C# libs
for /r %csModuleDir% %%x in (Ambeth.*.dll Ambeth.*.pdb Minerva.*.dll Minerva.*.pdb) do (
  copy "%%x" "%csSrcDir%\" > nul
)

rem Copy all external library libs
for /r %csModuleDir% %%x in (Castle.Core.dll Telerik.Windows.Controls.dll Telerik.Windows.Controls.Navigation.dll) do (
  copy "%%x" "%csLibDir%\" > nul
)

rem From Jenkins Job
set resultType=tcs
set javaModules=jambeth-bytecode,jambeth-cache,jambeth-cache-bytecode,jambeth-cache-datachange,jambeth-datachange,jambeth-event,jambeth-event-datachange,jambeth-filter,jambeth-ioc,jambeth-log,jambeth-mapping,jambeth-merge,jambeth-merge-bytecode,jambeth-privilege,jambeth-security,jambeth-service,jambeth-testutil,jambeth-util,jambeth-xml
set csModules=Ambeth.Bytecode,Ambeth.Cache,Ambeth.Cache.Bytecode,Ambeth.CacheDataChange,Ambeth.DataChange,Ambeth.Event,Ambeth.Event.DataChange,Ambeth.Filter,Ambeth.IoC,Ambeth.Log,Ambeth.Mapping,Ambeth.Merge,Ambeth.Merge.Bytecode,Ambeth.Privilege,Ambeth.Security,Ambeth.Service,Ambeth.TestUtil,Ambeth.Util,Ambeth.Xml

rem Create xml containing the description of the Java code
java -cp "%binDir%\JavaClassbrowser.jar" -DjarFolders="%javaSrcDir%" -DlibraryJarFolders="%javaLibDir%" -DtargetPath="%dataDir%" -DmoduleRootPath="%javaModuleDir%" -DmodulesToBeAnalyzed="%javaModules%" de.osthus.classbrowser.java.Program

rem Create xml containing the description of the C# code
"%binDir%\CsharpClassbrowser.exe" -assemblyPaths="%csSrcDir%" -libraryAssemblyPaths="%csLibDir%" -targetPath="%dataDir%" -moduleRootPath="%csModuleDir%" -modulesToBeAnalyzed="%csModules%" -skipModuleScan="%csSkipModuleScan%"

:end
