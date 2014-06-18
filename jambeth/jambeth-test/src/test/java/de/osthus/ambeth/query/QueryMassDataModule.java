package de.osthus.ambeth.query;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.proxy.IProxyFactory;

public class QueryMassDataModule implements IInitializingModule
{
	protected IProxyFactory proxyFactory;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(IQueryEntityCRUD.class).autowireable(IQueryEntityCRUD.class);

		IBeanConfiguration queryBeanBC = beanContextFactory.registerBean("myQuery1", QueryBean.class);
		queryBeanBC.propertyValue("EntityType", QueryEntity.class);
		queryBeanBC.propertyValue("QueryCreator", new IQueryCreator()
		{
			@Override
			public <T> IQuery<T> createCustomQuery(IQueryBuilder<T> qb)
			{
				return qb.build();
			}
		});
	}

	public void setProxyFactory(IProxyFactory proxyFactory)
	{
		this.proxyFactory = proxyFactory;
	}
}