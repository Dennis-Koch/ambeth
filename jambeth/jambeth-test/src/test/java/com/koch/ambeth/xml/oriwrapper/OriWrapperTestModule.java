package com.koch.ambeth.xml.oriwrapper;

import com.koch.ambeth.cache.util.CacheHelper;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.util.ICacheHelper;

public class OriWrapperTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("oriWrapperTestBed", OriWrapperTestBed.class).autowireable(OriWrapperTestBed.class);

		beanContextFactory.registerBean("cacheHelper", CacheHelper.class).autowireable(ICacheHelper.class);
	}
}
