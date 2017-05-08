@echo on

SETLOCAL
IF NOT DEFINED BUNDLE_HOME ( call "..\..\..\..\setenv.bat" )

cd %SOURCE_HOME%\ambeth\doc\reference-manual

@call mvn -f ../extendables-scanner clean compile
@call mvn -f ../extendables-scanner exec:java "-Dexec.args=scan-path=""../reference-manual/target/data"" source-path=""../../ambeth;../../jambeth"" target-all-dir=""../reference-manual"" target-extendable-tex-dir=""${target-all-dir}/extendable"" target-properties-tex-dir=""${target-all-dir}/configuration"" target-feature-tex-dir=""${target-all-dir}/feature"" target-module-tex-dir=""${target-all-dir}/module"" target-annotation-tex-dir=""${target-all-dir}/annotation"" "
@pause

