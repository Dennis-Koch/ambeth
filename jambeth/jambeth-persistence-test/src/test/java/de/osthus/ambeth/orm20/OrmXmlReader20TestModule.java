package de.osthus.ambeth.orm20;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.DefaultProxyHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.orm.OrmXmlReader20;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.util.XmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

public class OrmXmlReader20TestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(OrmXmlReader20.class).autowireable(OrmXmlReader20.class);
		beanContextFactory.registerBean("databaseDummy", DatabaseDummy.class).autowireable(IDatabase.class);
		beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);
		beanContextFactory.registerBean("proxyHelper", DefaultProxyHelper.class).autowireable(IProxyHelper.class);
	}
}
