@ECHO OFF
@call mvn -f ../extendables-scanner clean compile
@call mvn -f ../extendables-scanner exec:java -Dexec.mainClass="de.osthus.ambeth.extscanner.Main" "-Dexec.args=scan-path=""../../ambeth-integrity/de.osthus.classbrowser.solution/target/data"" target-tex-file=""../reference-manual/all-extendables.tex"" target-extendable-tex-dir=""../reference-manual/extendable"" properties-tex-file=""../reference-manual/all-configurations.tex"" target-properties-tex-dir=""../reference-manual/configuration"" source-path=""../../ambeth;../../jambeth"" "
@pause

