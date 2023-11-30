package com.koch.ambeth.dot.ioc;

import io.toolisticon.spiap.api.SpiService;
import com.koch.ambeth.dot.DotUtil;
import com.koch.ambeth.dot.IDotUtil;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class DotModule implements IFrameworkModule {
    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean(DotUtil.class).autowireable(IDotUtil.class);
    }
}
