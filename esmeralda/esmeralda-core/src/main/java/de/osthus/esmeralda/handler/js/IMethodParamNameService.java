package de.osthus.esmeralda.handler.js;

public interface IMethodParamNameService
{
	String[] getConstructorParamNames(String fqClassName, String... fqParamClassNames);

	String[] getMethodParamNames(String fqClassName, String methodName, String... fqParamClassNames);

	String[] getConstructorParamClassNames(String fqClassName, String... fqParamClassNames);

	String[] getMethodParamClassNames(String fqClassName, String methodName, String... fqParamClassNames);
}