package com.koch.ambeth.util.proxy;

import net.sf.cglib.proxy.MethodInterceptor;

public interface ICascadedInterceptor extends MethodInterceptor
{
	Object getTarget();

	void setTarget(Object obj);
}
