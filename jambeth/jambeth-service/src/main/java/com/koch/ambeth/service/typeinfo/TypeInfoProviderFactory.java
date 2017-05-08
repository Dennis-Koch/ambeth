package com.koch.ambeth.service.typeinfo;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfoProviderFactory;

public class TypeInfoProviderFactory implements ITypeInfoProviderFactory, IInitializingBean {
	protected IServiceContext serviceContext;

	protected Class<? extends TypeInfoProvider> typeInfoProviderType;

	@Override
	public void afterPropertiesSet() throws Throwable {
		ParamChecker.assertNotNull(serviceContext, "ServiceContext");
		ParamChecker.assertNotNull(typeInfoProviderType, "TypeInfoProviderType");
	}

	public void setServiceContext(IServiceContext serviceContext) {
		this.serviceContext = serviceContext;
	}

	public void setTypeInfoProviderType(Class<? extends TypeInfoProvider> typeInfoProviderType) {
		this.typeInfoProviderType = typeInfoProviderType;
	}

	@Override
	public ITypeInfoProvider createTypeInfoProvider() {
		return serviceContext.registerBean(typeInfoProviderType)
				.propertyValue("Synchronized", Boolean.FALSE).finish();
	}
}
