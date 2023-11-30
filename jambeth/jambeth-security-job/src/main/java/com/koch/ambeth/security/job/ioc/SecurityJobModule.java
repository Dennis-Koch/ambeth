package com.koch.ambeth.security.job.ioc;

/*-
 * #%L
 * jambeth-security-job
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
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.job.IJobExtendable;
import com.koch.ambeth.security.job.threading.ThreadPoolRefreshJob;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class SecurityJobModule implements IFrameworkModule {
    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        IBeanConfiguration threadPoolRefreshJob = beanContextFactory.registerBean(ThreadPoolRefreshJob.class)//
                                                                    .propertyRef("ThreadPool", IocModule.THREAD_POOL_NAME);
        beanContextFactory.link(threadPoolRefreshJob).to(IJobExtendable.class).with("threadPool-refresh", "* * * * * *").optional();
    }
}
