package de.osthus.esmeralda.handler.js;

public interface IMethodParamNameService
{
	String[] getConstructorParamNames(String fqClassName, String... fqParamClassNames);

	String[] getMethodParamNames(String fqClassName, String methodName, String... fqParamClassNames);
}