package com.koch.ambeth.dot.ioc;

import com.koch.ambeth.dot.DotUtil;
import com.koch.ambeth.dot.IDotUtil;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

@FrameworkModule
public class DotModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(DotUtil.class).autowireable(IDotUtil.class);
	}
}
