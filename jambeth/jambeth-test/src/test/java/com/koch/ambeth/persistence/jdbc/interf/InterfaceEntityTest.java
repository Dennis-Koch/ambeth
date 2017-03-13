package com.koch.ambeth.persistence.jdbc.interf;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;

@TestModule({})
@SQLStructure("structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/jdbc/interf/orm.xml")
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
