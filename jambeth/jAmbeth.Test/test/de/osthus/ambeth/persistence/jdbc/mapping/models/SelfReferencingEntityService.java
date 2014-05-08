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
import de.osthus.ambeth.persistence.jdbc.mapping.ISelfReferencingEntityService;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.util.ParamChecker;

@Service(ISelfReferencingEntityService.class)
@PersistenceContext
public class SelfReferencingEntityService implements ISelfReferencingEntityService, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(SelfReferencingEntityService.class)
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
	public SelfReferencingEntity getSelfReferencingEntityByName(String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnCollection(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnList(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnSet(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SelfReferencingEntity[] getSelfReferencingEntitiesByNamesReturnArray(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SelfReferencingEntity getSelfReferencingEntityByNames(Collection<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SelfReferencingEntity getSelfReferencingEntityByNames(List<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SelfReferencingEntity getSelfReferencingEntityByNames(String... names)
	{
		throw new UnsupportedOperationException();
	}

	public void test(String name)
	{
		ITable aieTable = database.getTableByType(SelfReferencingEntity.class);
		ArrayList<String> names = new ArrayList<String>();
		names.add(name);
		IVersionCursor selectVersion = aieTable.selectVersion("Name", names);
		System.out.println(selectVersion);
	}

	@Override
	public void updateSelfReferencingEntity(SelfReferencingEntity selfReferencingEntity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteSelfReferencingEntity(SelfReferencingEntity selfReferencingEntity)
	{
		throw new UnsupportedOperationException();
	}
}
