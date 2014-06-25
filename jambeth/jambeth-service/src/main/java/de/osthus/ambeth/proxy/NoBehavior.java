package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

public class NoBehavior implements IMethodLevelBehavior<Object>
{
	@Override
	public Object getBehaviourOfMethod(Method method)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getDefaultBehaviour()
	{
		throw new UnsupportedOperationException();
	}
}
