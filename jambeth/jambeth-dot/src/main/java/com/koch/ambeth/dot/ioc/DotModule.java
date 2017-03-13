package com.koch.ambeth.dot.ioc;

import com.koch.ambeth.dot.DotToImage;
import com.koch.ambeth.dot.IDotToImage;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class DotModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(DotToImage.class).autowireable(IDotToImage.class);
	}
}
