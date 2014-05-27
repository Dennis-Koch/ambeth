package de.osthus.ambeth.ioc.threadlocal;

import java.util.List;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IBeanPreProcessor;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.config.IPropertyConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.util.ParamChecker;

public class ThreadLocalCleanupPreProcessor implements IInitializingBean, IBeanPreProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IThreadLocalCleanupBeanExtendable threadLocalCleanupBeanExtendable;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(threadLocalCleanupBeanExtendable, "threadLocalCleanupBeanExtendable");
	}

	public void setThreadLocalCleanupBeanExtendable(IThreadLocalCleanupBeanExtendable threadLocalCleanupBeanExtendable)
	{
		this.threadLocalCleanupBeanExtendable = threadLocalCleanupBeanExtendable;
	}

	@Override
	public void preProcessProperties(IBeanContextFactory beanContextFactory, IProperties props, String beanName, Object service, Class<?> beanType,
			List<IPropertyConfiguration> propertyConfigs, IPropertyInfo[] properties)
	{
		if (service instanceof IThreadLocalCleanupBean)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Registered bean '" + beanName + "' to " + IThreadLocalCleanupBeanExtendable.class.getSimpleName() + " because it implements "
						+ IThreadLocalCleanupBean.class.getSimpleName());
			}
			beanContextFactory.link(service).to(IThreadLocalCleanupBeanExtendable.class);
		}
	}
}
