package com.koch.ambeth.service.rest.ioc;

/*-
 * #%L
 * jambeth-service-rest
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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.service.remote.IClientServiceFactory;
import com.koch.ambeth.service.rest.AuthenticationHolder;
import com.koch.ambeth.service.rest.HttpClientProvider;
import com.koch.ambeth.service.rest.IAuthenticationHolder;
import com.koch.ambeth.service.rest.IHttpClientProvider;
import com.koch.ambeth.service.rest.RESTClientServiceFactory;
import com.koch.ambeth.service.rest.config.RESTConfigurationConstants;

public class ServiceRESTModule implements IInitializingModule {
	@Autowired(optional = true)
	protected IAuthenticationHolder authenticationHolder;

	@Property(name = RESTConfigurationConstants.AuthenticationHolderType, mandatory = false)
	protected Class<?> authenticationHolderType;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(RESTClientServiceFactory.class)
				.autowireable(IClientServiceFactory.class);

		if (authenticationHolder == null) {
			if (authenticationHolderType == null) {
				authenticationHolderType = AuthenticationHolder.class;
			}
			beanContextFactory.registerBean(authenticationHolderType)
					.autowireable(IAuthenticationHolder.class);
		}
		beanContextFactory.registerBean(HttpClientProvider.class)
				.autowireable(IHttpClientProvider.class);

	}
}
