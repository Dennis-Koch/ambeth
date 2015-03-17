package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.rdf.IRdfUtils;
import de.osthus.ambeth.rdf.RdfUtils;

@FrameworkModule
public class RdfModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(RdfUtils.class).autowireable(IRdfUtils.class);
	}
}
