package de.osthus.ambeth.persistence.jdbc.alternateid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IServiceUtil;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;

@Service(IAlternateIdEntityService.class)
@PersistenceContext
public class AlternateIdEntityService implements IAlternateIdEntityService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IServiceUtil serviceUtil;

	@Override
	public AlternateIdEntity getAlternateIdEntityByName(String name)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnCollection(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnList(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnSet(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AlternateIdEntity[] getAlternateIdEntitiesByNamesReturnArray(String... names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AlternateIdEntity getAlternateIdEntityByNames(Collection<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AlternateIdEntity getAlternateIdEntityByNames(List<String> names)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AlternateIdEntity getAlternateIdEntityByNames(String... names)
	{
		throw new UnsupportedOperationException();
	}

	public void test(String name)
	{
		ITable aieTable = database.getTableByType(AlternateIdEntity.class);
		ArrayList<String> names = new ArrayList<String>();
		names.add(name);
		IVersionCursor selectVersion = aieTable.selectVersion("Name", names);
		System.out.println(selectVersion);
	}

	@Override
	public void updateAlternateIdEntity(AlternateIdEntity alternateIdEntity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAlternateIdEntity(AlternateIdEntity alternateIdEntity)
	{
		throw new UnsupportedOperationException();
	}
}
