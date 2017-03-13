package com.koch.ambeth.persistence.jdbc.connector;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;

public interface IConnector
{
	void handleProperties(Properties props, String databaseProtocol);

	void handleProd(IBeanContextFactory beanContextFactory, String databaseProtocol);
}
