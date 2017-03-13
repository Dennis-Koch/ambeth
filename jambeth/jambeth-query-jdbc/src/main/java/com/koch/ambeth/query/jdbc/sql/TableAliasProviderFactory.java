package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;

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
		return beanContext.registerBean(TableAliasProvider.class).finish();
	}
}
