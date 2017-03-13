package com.koch.ambeth.server.helloworld;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestProperties;

@SQLStructure("/create_hello_world_tables.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/helloworld/helloworld_orm.xml")
public abstract class AbstractHelloWorldTest extends AbstractInformationBusWithPersistenceTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;
}
