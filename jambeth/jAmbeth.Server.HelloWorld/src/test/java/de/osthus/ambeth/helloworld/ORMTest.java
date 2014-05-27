package de.osthus.ambeth.helloworld;

import org.junit.Test;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;

@SQLStructure("/create_hello_world_tables.sql")
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/helloworld/helloworld_orm.xml")
public class ORMTest extends AbstractPersistenceTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Test
	public void testOrm()
	{

	}
}
