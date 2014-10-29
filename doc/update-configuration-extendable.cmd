@ECHO OFF
@SET MVN_PATH=%USERPROFILE%\.m2\repository
@SET AMBETH_VERSION=2.2.47
@SET AMBETH_UTIL=%MVN_PATH%\de\osthus\ambeth\jambeth-util\%AMBETH_VERSION%\jambeth-util-%AMBETH_VERSION%.jar
@SET AMBETH_LOG=%MVN_PATH%\de\osthus\ambeth\jambeth-log\%AMBETH_VERSION%\jambeth-log-%AMBETH_VERSION%.jar
@SET AMBETH_IOC=%MVN_PATH%\de\osthus\ambeth\jambeth-ioc\%AMBETH_VERSION%\jambeth-ioc-%AMBETH_VERSION%.jar
@SET AMBETH_XML=%MVN_PATH%\de\osthus\ambeth\jambeth-xml\%AMBETH_VERSION%\jambeth-xml-%AMBETH_VERSION%.jar
@SET CGLIB=%MVN_PATH%\cglib\cglib-nodep\2.2.2\cglib-nodep-2.2.2.jar
@SET JAVASSIST=%MVN_PATH%\org\javassist\javassist\3.16.1-GA\javassist-3.18.2-GA.jar
@SET SERVLET=%MVN_PATH%/javax/servlet/javax.servlet-api/3.0.1/javax.servlet-api-3.0.1.jar
@cd extendables-scanner
@call mvn clean compile
@cd ..
@java  -cp "%AMBETH_UTIL%;%AMBETH_LOG%;%AMBETH_IOC%;%AMBETH_XML%;%CGLIB%;%JAVASSIST%;%SERVLET%;extendables-scanner/target/classes" de.osthus.ambeth.extscanner.Main scan-path="../jambeth;../ambeth" target-tex-file="all-extendables.tex" target-extendable-tex-dir="extendable" properties-tex-file="all-configurations.tex" target-properties-tex-dir="configuration"
@pause