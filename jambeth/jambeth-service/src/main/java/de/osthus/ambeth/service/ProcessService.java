package de.osthus.ambeth.service;

import java.lang.reflect.Method;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;

public class ProcessService implements IProcessService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IServiceByNameProvider serviceByNameProvider;

	@Override
	public Object invokeService(IServiceDescription serviceDescription)
	{
		ParamChecker.assertParamNotNull(serviceDescription, "serviceDescription");

		Object service = serviceByNameProvider.getService(serviceDescription.getServiceName());

		if (service == null)
		{
			throw new IllegalStateException("No service with name '" + serviceDescription.getServiceName() + "' found");
		}
		Method method = serviceDescription.getMethod(service.getClass(), objectCollector);
		if (method == null)
		{
			throw new IllegalStateException("Requested method not found on service '" + serviceDescription.getServiceName() + "'");
		}
		try
		{
			return method.invoke(service, serviceDescription.getArguments());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
