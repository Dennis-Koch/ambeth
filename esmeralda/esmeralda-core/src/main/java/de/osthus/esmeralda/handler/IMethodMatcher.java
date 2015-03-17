package de.osthus.esmeralda.handler;

public interface IMethodMatcher
{
	String resolveMethodReturnType(String currOwner, String methodName, String... argTypes);
}