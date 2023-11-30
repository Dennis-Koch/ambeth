package com.koch.ambeth.expr.exp4j.ioc;

/*-
 * #%L
 * jambeth-expr-exp4j
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
import com.koch.ambeth.expr.IEntityPropertyExpressionResolver;
import com.koch.ambeth.expr.exp4j.EntityPropertyExpressionResolver;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class Exp4jModule implements IFrameworkModule {
    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean(EntityPropertyExpressionResolver.class).autowireable(IEntityPropertyExpressionResolver.class);
    }
}
