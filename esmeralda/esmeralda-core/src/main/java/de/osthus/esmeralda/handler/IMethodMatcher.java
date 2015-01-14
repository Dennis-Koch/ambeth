package de.osthus.esmeralda.handler;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface IMethodMatcher
{

	String resolveMethodReturnType(String currOwner, String methodName, String... argTypes);

}