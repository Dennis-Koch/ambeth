package de.osthus.ambeth.persistence.jdbc.mapping.models;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IServiceUtil;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.jdbc.mapping.IOneToManyEntityService;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

@Service(IOneToManyEntityService.class)
@PersistenceContext
public class OneToManyEntityService implements IOneToManyEntityService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(OneToManyEntityService.class)
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
	public OneToManyEntity getOneToManyEntityByName(String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<OneToManyEntity> getOneToManyEntitiesByNamesReturnCollection(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OneToManyEntity> getOneToManyEntitiesByNamesReturnList(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<OneToManyEntity> getOneToManyEntitiesByNamesReturnSet(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OneToManyEntity[] getOneToManyEntitiesByNamesReturnArray(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OneToManyEntity getOneToManyEntityByNames(Collection<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OneToManyEntity getOneToManyEntityByNames(List<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public OneToManyEntity getOneToManyEntityByNames(String... names)
	{
		throw new UnsupportedOperationException();
	}

	public void test(String name)
	{
		ITable aieTable = database.getTableByType(OneToManyEntity.class);
		ArrayList<String> names = new ArrayList<String>();
		names.add(name);
		IVersionCursor selectVersion = aieTable.selectVersion("Name", names);
		System.out.println(selectVersion);
	}

	@Override
	public void updateOneToManyEntity(OneToManyEntity oneToManyEntity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteOneToManyEntity(OneToManyEntity oneToManyEntity)
	{
		throw new UnsupportedOperationException();
	}
}
