package com.koch.ambeth.merge.ioc;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import io.toolisticon.spiap.api.SpiService;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IMergeListenerExtendable;
import com.koch.ambeth.merge.changecontroller.AbstractRule;
import com.koch.ambeth.merge.changecontroller.ChangeController;
import com.koch.ambeth.merge.changecontroller.IChangeController;
import com.koch.ambeth.merge.changecontroller.IChangeControllerExtendable;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class ChangeControllerModule implements IFrameworkModule {
    public static <T> IBeanConfiguration registerRule(IBeanContextFactory contextFactory, Class<? extends AbstractRule<T>> validatorClass, Class<T> validatedEntity) {
        IBeanConfiguration beanConfig = contextFactory.registerBean(validatorClass);
        contextFactory.link(beanConfig).to(IChangeControllerExtendable.class).with(validatedEntity);
        return beanConfig;
    }

    @Property(name = MergeConfigurationConstants.edblActive, defaultValue = "true")
    protected Boolean edblActive;

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        if (Boolean.TRUE.equals(edblActive)) {
            IBeanConfiguration ccBean = beanContextFactory.registerAnonymousBean(ChangeController.class);
            ccBean.autowireable(IChangeController.class, IChangeControllerExtendable.class);
            beanContextFactory.link(ccBean).to(IMergeListenerExtendable.class);
        }

    }

}
