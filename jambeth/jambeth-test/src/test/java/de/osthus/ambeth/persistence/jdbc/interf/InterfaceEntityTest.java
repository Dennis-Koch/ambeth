package de.osthus.ambeth.persistence.jdbc.interf;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@TestModule({})
@SQLStructure("structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/jdbc/interf/orm.xml")
public class InterfaceEntityTest extends AbstractInformationBusWithPersistenceTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IMergeProcess mergeProcess;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(mergeProcess, "MergeProcess");
	}

	public void setMergeProcess(IMergeProcess mergeProcess)
	{
		this.mergeProcess = mergeProcess;
	}

	protected ITestEntity createEntity(String name)
	{
		ITestEntity entity = entityFactory.createEntity(ITestEntity.class);
		entity.setName(name);

		mergeProcess.process(entity, null, null, null);
		return entity;
	}

	@Test
	public void createEntity()
	{
		String myName = "name55";
		ITestEntity entity = createEntity(myName);

		Assert.assertTrue("Wrong id", entity.getId() > 0);
		Assert.assertEquals("Wrong version!", (short) 1, entity.getVersion());
	}

	@Test
	public void editEntity()
	{
		String myName = "name55";
		ITestEntity entity = createEntity(myName);

		entity.setName(myName + "x");
		mergeProcess.process(entity, null, null, null);
		Assert.assertEquals("Wrong version!", (short) 2, entity.getVersion());
	}
}
