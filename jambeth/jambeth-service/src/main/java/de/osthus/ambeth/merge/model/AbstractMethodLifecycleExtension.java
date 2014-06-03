package de.osthus.ambeth.merge.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import de.osthus.ambeth.util.ParamChecker;

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
