package de.osthus.ambeth.util.prefetch;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.jdbc.JDBCResultSet;
import de.osthus.ambeth.persistence.jdbc.connection.DefaultStatementPerformanceLogger;
import de.osthus.ambeth.persistence.jdbc.connection.IStatementPerformanceReport;
import de.osthus.ambeth.persistence.jdbc.connection.LogStatementInterceptor;
import de.osthus.ambeth.sensor.ISensorReceiverExtendable;
import de.osthus.ambeth.util.setup.IDatasetBuilderExtendable;

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
