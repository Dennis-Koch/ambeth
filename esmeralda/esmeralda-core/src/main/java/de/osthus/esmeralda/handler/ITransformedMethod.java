package de.osthus.esmeralda.handler;

public interface ITransformedMethod
{
	String getOwner();

	String getName();

	String[] getArgumentTypes();

	boolean isStatic();

	boolean isPropertyInvocation();
}
