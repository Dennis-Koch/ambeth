package com.koch.ambeth.filter;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.DefaultProxyHelper;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.util.XmlConfigUtil;
import com.koch.ambeth.persistence.filter.FilterToQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.xml.IXmlConfigUtil;

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
