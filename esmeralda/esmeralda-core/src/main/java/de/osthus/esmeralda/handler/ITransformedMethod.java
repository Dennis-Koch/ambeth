package de.osthus.esmeralda.handler;

public interface ITransformedMethod
{
	String getOwner();

	String getName();

	String[] getArgumentTypes();

	Boolean isWriteOwner();

	boolean isOwnerAType();

	boolean isStatic();

	boolean isPropertyInvocation();

	boolean isIndexedInvocation();

	IMethodParameterProcessor getParameterProcessor();
}
