package com.koch.ambeth.service.merge.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public abstract class AbstractMethodLifecycleExtension implements IEntityLifecycleExtension, IInitializingBean
{
	protected static final Object[] EMPTY_ARGS = new Object[0];

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected Method method;

	protected MethodAccess methodAccess;

	protected int methodIndex;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(method, "method");
		if ((method.getModifiers() & Modifier.PRIVATE) == 0)
		{
			methodAccess = MethodAccess.get(method.getDeclaringClass());
			methodIndex = methodAccess.getIndex(method.getName(), method.getParameterTypes());
		}
	}

	public void setMethod(Method method)
	{
		this.method = method;
	}

	protected void callMethod(Object entity, String message)
	{
		try
		{
			if (methodAccess != null)
			{
				methodAccess.invoke(entity, methodIndex, EMPTY_ARGS);
			}
			else
			{
				method.invoke(entity, EMPTY_ARGS);
			}
		}
		catch (Exception e)
		{
			Class<?> entityType = entityMetaDataProvider.getMetaData(entity.getClass()).getEntityType();
			throw RuntimeExceptionUtil.mask(e, "Error occured while handling " + message + " method of entity type " + entityType.getName());
		}
	}
}
