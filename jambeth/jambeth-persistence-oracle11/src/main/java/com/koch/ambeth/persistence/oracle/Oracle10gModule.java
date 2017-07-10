package com.koch.ambeth.persistence.oracle;

/*-
 * #%L
 * jambeth-persistence-oracle11
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

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;

public class Oracle10gModule extends Oracle10gSimpleModule {
	public static boolean handlesDatabaseProtocol(String databaseProtocol) {
		return databaseProtocol.toLowerCase().startsWith("jdbc:oracle");
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		super.afterPropertiesSet(beanContextFactory);

		beanContextFactory.registerBean("oracleConnectionExtension", OracleConnectionExtension.class)
				.autowireable(IConnectionExtension.class);
	}
}
