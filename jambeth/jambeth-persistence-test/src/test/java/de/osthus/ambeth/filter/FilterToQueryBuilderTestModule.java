package de.osthus.ambeth.filter;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.DefaultProxyHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.util.XmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

public class FilterToQueryBuilderTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(FilterToQueryBuilder.class).autowireable(FilterToQueryBuilder.class);
		beanContextFactory.registerBean("queryBuilderDummy", QueryBuilderFactoryDummy.class).autowireable(IQueryBuilderFactory.class);
		beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);
		beanContextFactory.registerBean("proxyHelper", DefaultProxyHelper.class).autowireable(IProxyHelper.class);
	}
}
