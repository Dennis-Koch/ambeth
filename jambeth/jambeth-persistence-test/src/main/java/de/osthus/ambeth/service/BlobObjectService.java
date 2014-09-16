package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.BlobObject;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IServiceUtil;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

@Service(IBlobObjectService.class)
@PersistenceContext
public class BlobObjectService implements IBlobObjectService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(BlobObjectService.class)
	private ILogger log;

	protected IDatabase database;

	protected IServiceUtil serviceUtil;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(database, "database");
		ParamChecker.assertNotNull(serviceUtil, "serviceUtil");
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	public void setServiceUtil(IServiceUtil serviceUtil)
	{
		this.serviceUtil = serviceUtil;
	}

	@Override
	public List<BlobObject> getAllBlobObjects()
	{
		ITable blobObjectTable = database.getTableByType(BlobObject.class);

		ArrayList<BlobObject> list = new ArrayList<BlobObject>();
		serviceUtil.loadObjectsIntoCollection(list, BlobObject.class, blobObjectTable.selectAll());
		return list;
	}

	@Override
	public BlobObject getBlobObject(Integer id)
	{
		ITable blobObjectTable = database.getTableByType(BlobObject.class);

		ArrayList<BlobObject> list = new ArrayList<BlobObject>(1);
		List<Object> ids = new ArrayList<Object>();
		ids.add(id);
		serviceUtil.loadObjectsIntoCollection(list, BlobObject.class, blobObjectTable.selectVersion(ids));
		if (list.size() > 0)
		{
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<BlobObject> getBlobObjects(Integer... id)
	{
		ITable blobObjectTable = database.getTableByType(BlobObject.class);

		ArrayList<BlobObject> list = new ArrayList<BlobObject>(id.length);
		serviceUtil.loadObjectsIntoCollection(list, BlobObject.class, blobObjectTable.selectVersion(new ArrayList<Object>(id)));
		return list;
	}

	@Override
	public void updateBlobObject(BlobObject blobObject)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteBlobObject(BlobObject blobObject)
	{
		throw new UnsupportedOperationException();
	}
}
