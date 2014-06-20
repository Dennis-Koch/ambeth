package de.osthus.ambeth.persistence.jdbc.lob;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.model.ClobObject;
import de.osthus.ambeth.persistence.jdbc.lob.ClobTest.ClobTestModule;
import de.osthus.ambeth.service.ClobObjectService;
import de.osthus.ambeth.service.IClobObjectService;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("clob_data.sql")
@SQLStructure("clob_structure.sql")
@TestModule(ClobTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/jdbc/lob/clob_orm.xml")
public class ClobTest extends AbstractPersistenceTest
{
	public static class ClobTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAutowireableBean(IClobObjectService.class, ClobObjectService.class);
		}
	}

	protected ClobObject createAndSaveClob(int size)
	{
		IClobObjectService clobObjectService = beanContext.getService(IClobObjectService.class);

		ClobObject clob = entityFactory.createEntity(ClobObject.class);
		clob.setContent(new char[size]);

		clobObjectService.updateClobObject(clob);
		return clob;
	}

	protected ClobObject createAndSaveClob(char[] content)
	{
		IClobObjectService clobObjectService = beanContext.getService(IClobObjectService.class);

		ClobObject clob = entityFactory.createEntity(ClobObject.class);
		clob.setContent(content);

		clobObjectService.updateClobObject(clob);
		return clob;
	}

	@Test
	public void createClob()
	{
		int clobSize = 1234;
		ClobObject clob = createAndSaveClob(clobSize);

		Assert.assertFalse("Wrong id", clob.getId() == 0);
		Assert.assertEquals("Wrong version!", (short) 1, clob.getVersion());
		assertNotNull("Content is null", clob.getContent());
		Assert.assertEquals("Wrong size", clobSize, clob.getContent().length);
	}

	@Test
	public void updateClob()
	{
		IRootCache rootCache = beanContext.getService(IRootCache.class);

		IClobObjectService clobObjectService = beanContext.getService(IClobObjectService.class);

		ClobObject clob = createAndSaveClob(1234);

		short v1 = clob.getVersion();
		int newBlobSize = 23450;

		clob.setContent(new char[newBlobSize]);
		clobObjectService.updateClobObject(clob);

		short v2 = clob.getVersion();

		Assert.assertNotSame("Version should be different", v1, v2);

		rootCache.clear(); // Clear the whole cache

		List<ClobObject> clobObjects = clobObjectService.getClobObjects(clob.getId());

		Assert.assertNotNull("Blob collection is not valid", clobObjects);
		Assert.assertSame("Blob collection is not valid", 1, clobObjects.size());

		ClobObject reloadedBlob = clobObjects.get(0);
		Assert.assertNotNull("Reloaded clob must be valid", reloadedBlob);

		Assert.assertNotNull("Blob must have been valid", reloadedBlob.getContent());
		Assert.assertEquals("Blob length must have been correct", newBlobSize, reloadedBlob.getContent().length);
	}

	@Test
	public void updateClobWithContent()
	{
		IRootCache rootCache = beanContext.getService(IRootCache.class);

		IClobObjectService clobObjectService = beanContext.getService(IClobObjectService.class);

		char[] content = "test content".toCharArray();
		ClobObject clob = createAndSaveClob(content);

		char[] newContent = "new test content".toCharArray();

		clob.setContent(newContent);
		clobObjectService.updateClobObject(clob);

		rootCache.clear(); // Clear the whole cache

		List<ClobObject> clobObjects = clobObjectService.getClobObjects(clob.getId());

		Assert.assertNotNull("Blob collection is not valid", clobObjects);
		Assert.assertSame("Blob collection is not valid", 1, clobObjects.size());

		ClobObject reloadedBlob = clobObjects.get(0);
		Assert.assertNotNull("Reloaded clob must be valid", reloadedBlob);

		Assert.assertNotNull("Blob must have been valid", reloadedBlob.getContent());
		Assert.assertArrayEquals("Blob content must have been correct", newContent, reloadedBlob.getContent());
	}

	@Test
	public void deleteClob()
	{
		IClobObjectService clobObjectService = beanContext.getService(IClobObjectService.class);

		ClobObject clob = createAndSaveClob(1234);

		clobObjectService.deleteClobObject(clob);

		Assert.assertEquals("Wrong id", 0, clob.getId());
		Assert.assertEquals("Wrong version", 0, clob.getVersion());

		// rootCache.clear(); // Clear the whole cache

		List<ClobObject> clobObjects = clobObjectService.getClobObjects(clob.getId());

		Assert.assertNotNull("Blob collection is not valid", clobObjects);
		Assert.assertEquals("Blob collection is not valid", 0, clobObjects.size());
	}
}
