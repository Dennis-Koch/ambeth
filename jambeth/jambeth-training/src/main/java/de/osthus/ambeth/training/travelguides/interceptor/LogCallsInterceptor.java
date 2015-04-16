package de.osthus.ambeth.training.travelguides.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;

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
