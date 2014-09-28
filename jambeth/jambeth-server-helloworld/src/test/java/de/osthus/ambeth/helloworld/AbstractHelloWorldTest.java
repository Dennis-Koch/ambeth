package de.osthus.ambeth.helloworld;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@SQLStructure("/create_hello_world_tables.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/helloworld/helloworld_orm.xml")
public abstract class AbstractHelloWorldTest extends AbstractPersistenceTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;
}
