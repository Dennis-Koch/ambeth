package com.koch.ambeth.training.travelguides.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.proxy.CascadedInterceptor;

public class LogCallsInterceptor extends CascadedInterceptor
{

	@LogInstance
	ILogger log;

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		log.info("call: " + method.getName());

		return invokeTarget(obj, method, args, proxy);
	}
}
