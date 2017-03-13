package com.koch.ambeth.service.proxy;

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
