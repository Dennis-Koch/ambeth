package de.osthus.ambeth.persistence.jdbc.array;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IServiceUtil;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

@Service(IArrayObjectService.class)
@PersistenceContext
public class ArrayObjectService implements IArrayObjectService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(ArrayObjectService.class)
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
	public List<ArrayObject> getAllArrayObjects()
	{
		ITable table = database.getTableByType(ArrayObject.class);

		ArrayList<ArrayObject> list = new ArrayList<ArrayObject>();
		serviceUtil.loadObjectsIntoCollection(list, ArrayObject.class, table.selectAll());
		return list;
	}

	@Override
	public ArrayObject getArrayObject(Integer id)
	{
		ITable table = database.getTableByType(ArrayObject.class);

		ArrayList<ArrayObject> list = new ArrayList<ArrayObject>(1);
		List<Object> ids = new ArrayList<Object>();
		ids.add(id);
		serviceUtil.loadObjectsIntoCollection(list, ArrayObject.class, table.selectVersion(ids));
		if (list.size() > 0)
		{
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<ArrayObject> getArrayObjects(Integer... id)
	{
		ITable table = database.getTableByType(ArrayObject.class);

		ArrayList<ArrayObject> list = new ArrayList<ArrayObject>(id.length);
		serviceUtil.loadObjectsIntoCollection(list, ArrayObject.class, table.selectVersion(new ArrayList<Object>(id)));
		return list;
	}

	@Override
	public void updateArrayObject(ArrayObject arrayObject)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteArrayObject(ArrayObject arrayObject)
	{
		throw new UnsupportedOperationException();
	}
}