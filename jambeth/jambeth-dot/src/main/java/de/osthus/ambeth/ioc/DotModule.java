package de.osthus.ambeth.ioc;

import de.osthus.ambeth.dot.DotToImage;
import de.osthus.ambeth.dot.IDotToImage;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class DotModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(DotToImage.class).autowireable(IDotToImage.class);
	}
}
