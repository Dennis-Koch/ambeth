package de.osthus.ambeth.persistence.jdbc.lob;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.model.BlobObject;
import de.osthus.ambeth.persistence.jdbc.lob.BlobTest.BlobTestModule;
import de.osthus.ambeth.service.BlobObjectService;
import de.osthus.ambeth.service.IBlobObjectService;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("blob_data.sql")
@SQLStructure("blob_structure.sql")
@TestModule(BlobTestModule.class)
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/jdbc/lob/blob_orm.xml")
public class BlobTest extends AbstractPersistenceTest
{
	public static class BlobTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAutowireableBean(IBlobObjectService.class, BlobObjectService.class);
		}
	}

	protected BlobObject createAndSaveBlob(int size)
	{
		IBlobObjectService blobObjectService = beanContext.getService(IBlobObjectService.class);

		BlobObject blob = entityFactory.createEntity(BlobObject.class);
		if (size < 0)
		{
			blob.setContent(null);
		}
		else
		{
			blob.setContent(new byte[size]);
		}

		blobObjectService.updateBlobObject(blob);
		return blob;
	}

	protected BlobObject createAndSaveBlob(byte[] content)
	{
		IBlobObjectService blobObjectService = beanContext.getService(IBlobObjectService.class);

		BlobObject blob = entityFactory.createEntity(BlobObject.class);
		blob.setContent(content);

		blobObjectService.updateBlobObject(blob);
		return blob;
	}

	@Test
	public void createNullBlob()
	{
		BlobObject blob = createAndSaveBlob(-1);

		Assert.assertFalse("Wrong id", blob.getId() == 0);
		Assert.assertEquals("Wrong version!", (short) 1, blob.getVersion());
		Assert.assertNull("Content must be null", blob.getContent());
	}

	@Test
	public void readNullBlob()
	{
		BlobObject blob = createAndSaveBlob(-1);
		beanContext.getService(IRootCache.class).clear();

		IBlobObjectService blobObjectService = beanContext.getService(IBlobObjectService.class);

		BlobObject blobObject = blobObjectService.getBlobObject(blob.getId());
		Assert.assertNotNull(blobObject);

	}

	@Test
	public void createBlob()
	{
		int blobSize = 1234;
		BlobObject blob = createAndSaveBlob(blobSize);

		Assert.assertFalse("Wrong id", blob.getId() == 0);
		Assert.assertEquals("Wrong version!", (short) 1, blob.getVersion());
		assertNotNull("Content is null", blob.getContent());
		Assert.assertEquals("Wrong size", blobSize, blob.getContent().length);
	}

	@Test
	public void updateBlob()
	{
		IRootCache rootCache = beanContext.getService(IRootCache.class);

		IBlobObjectService blobObjectService = beanContext.getService(IBlobObjectService.class);

		BlobObject blob = createAndSaveBlob(1234);

		short v1 = blob.getVersion();
		int newBlobSize = 23450;

		blob.setContent(new byte[newBlobSize]);
		blobObjectService.updateBlobObject(blob);

		short v2 = blob.getVersion();

		Assert.assertNotSame("Version should be different", v1, v2);

		rootCache.clear(); // Clear the whole cache

		List<BlobObject> blobObjects = blobObjectService.getBlobObjects(blob.getId());

		Assert.assertNotNull("Blob collection is not valid", blobObjects);
		Assert.assertSame("Blob collection is not valid", 1, blobObjects.size());

		BlobObject reloadedBlob = blobObjects.get(0);
		Assert.assertNotNull("Reloaded blob must be valid", reloadedBlob);

		Assert.assertNotNull("Blob must have been valid", reloadedBlob.getContent());
		Assert.assertEquals("Blob length must have been correct", newBlobSize, reloadedBlob.getContent().length);
	}

	@Test
	public void updateBlobWithContent()
	{
		IRootCache rootCache = beanContext.getService(IRootCache.class);

		IBlobObjectService blobObjectService = beanContext.getService(IBlobObjectService.class);

		byte[] content = { 1, 2, 3, 4, 5 };
		BlobObject blob = createAndSaveBlob(content);

		byte[] newContent = { 1, 1, 2, 3, 5, 8 };

		blob.setContent(newContent);
		blobObjectService.updateBlobObject(blob);

		rootCache.clear(); // Clear the whole cache

		List<BlobObject> blobObjects = blobObjectService.getBlobObjects(blob.getId());

		Assert.assertNotNull("Blob collection is not valid", blobObjects);
		Assert.assertSame("Blob collection is not valid", 1, blobObjects.size());

		BlobObject reloadedBlob = blobObjects.get(0);
		Assert.assertNotNull("Reloaded clob must be valid", reloadedBlob);

		Assert.assertNotNull("Blob must have been valid", reloadedBlob.getContent());
		Assert.assertArrayEquals("Blob content must have been correct", newContent, reloadedBlob.getContent());
	}

	@Test
	public void deleteBlob()
	{
		IBlobObjectService blobObjectService = beanContext.getService(IBlobObjectService.class);

		BlobObject blob = createAndSaveBlob(1234);

		blobObjectService.deleteBlobObject(blob);

		Assert.assertEquals("Wrong id", 0, blob.getId());
		Assert.assertEquals("Wrong version", 0, blob.getVersion());

		// rootCache.clear(); // Clear the whole cache

		List<BlobObject> blobObjects = blobObjectService.getAllBlobObjects();

		Assert.assertNotNull("Blob collection is not valid", blobObjects);
		Assert.assertEquals("Blob collection is not valid", 0, blobObjects.size());
	}
}
