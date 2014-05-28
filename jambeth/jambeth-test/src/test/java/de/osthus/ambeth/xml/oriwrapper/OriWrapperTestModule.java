package de.osthus.ambeth.xml.oriwrapper;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.util.CacheHelper;
import de.osthus.ambeth.util.ICacheHelper;

public class OriWrapperTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("oriWrapperTestBed", OriWrapperTestBed.class).autowireable(OriWrapperTestBed.class);

		beanContextFactory.registerBean("cacheHelper", CacheHelper.class).autowireable(ICacheHelper.class);
	}
}
