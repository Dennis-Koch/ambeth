package com.koch.ambeth.util.prefetch;

/*-
 * #%L
 * jambeth-test
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
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.util.setup.IDatasetBuilderExtendable;
import com.koch.ambeth.persistence.jdbc.JDBCResultSet;
import com.koch.ambeth.persistence.jdbc.connection.DefaultStatementPerformanceLogger;
import com.koch.ambeth.persistence.jdbc.connection.IStatementPerformanceReport;
import com.koch.ambeth.persistence.jdbc.connection.LogStatementInterceptor;
import com.koch.ambeth.util.sensor.ISensorReceiverExtendable;

public class PrefetchTestModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration prefetchTestSensorReceiver = beanContextFactory.registerBean(DefaultStatementPerformanceLogger.class).autowireable(
				IStatementPerformanceReport.class);
		beanContextFactory.link(prefetchTestSensorReceiver).to(ISensorReceiverExtendable.class).with(JDBCResultSet.SENSOR_NAME);
		beanContextFactory.link(prefetchTestSensorReceiver).to(ISensorReceiverExtendable.class).with(LogStatementInterceptor.SENSOR_NAME);

		IBeanConfiguration prefetchTestDataSetup = beanContextFactory.registerBean(PrefetchTestDataSetup.class).autowireable(PrefetchTestDataSetup.class);
		beanContextFactory.link(prefetchTestDataSetup).to(IDatasetBuilderExtendable.class);
	}
}
