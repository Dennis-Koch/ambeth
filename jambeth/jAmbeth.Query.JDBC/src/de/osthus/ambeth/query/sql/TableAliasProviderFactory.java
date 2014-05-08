package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class TableAliasProviderFactory implements IFactoryBean, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private IServiceContext beanContext;

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
		return beanContext.registerAnonymousBean(TableAliasProvider.class).finish();
	}
}
