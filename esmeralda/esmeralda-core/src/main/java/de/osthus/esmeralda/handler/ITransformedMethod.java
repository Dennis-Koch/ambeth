package de.osthus.esmeralda.handler;

public interface ITransformedMethod
{
	String getOwner();

	String getName();

	String[] getArgumentTypes();

	Boolean isWriteOwner();

	boolean isStatic();

	boolean isPropertyInvocation();

	IMethodParameterProcessor getParameterProcessor();
}
