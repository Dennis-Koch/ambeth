package com.koch.ambeth.util.prefetch;

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
