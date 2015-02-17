package de.osthus.ambeth.testutil;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public class PreparedStatementParamLoggerFactory implements IFactoryBean, IInitializingBean
{
	private static final Pattern SETTER = Pattern.compile("^set[A-Z].*");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Set<Method> paramSetters;

	protected IServiceContext beanContext;

	public PreparedStatementParamLoggerFactory()
	{
		Set<Method> paramSetters = new HashSet<Method>();

		Method[] methods = ReflectUtil.getMethods(PreparedStatement.class);
		for (int i = methods.length; i-- > 0;)
		{
			Method method = methods[i];
			if (!SETTER.matcher(method.getName()).matches())
			{
				continue;
			}

			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 2 || !int.class.equals(parameterTypes[0]))
			{
				continue;
			}

			paramSetters.add(method);
		}

		this.paramSetters = Collections.unmodifiableSet(paramSetters);
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(beanContext, "beanContext");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	@Override
	public Object getObject() throws Throwable
	{
		PreparedStatementParamLogger paramLogger = beanContext.registerBean(PreparedStatementParamLogger.class)
				.propertyValue("ParamSetters", paramSetters).finish();

		return paramLogger;
	}
}
