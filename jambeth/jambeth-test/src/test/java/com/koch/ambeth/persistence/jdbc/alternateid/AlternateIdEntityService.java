package com.koch.ambeth.persistence.jdbc.alternateid;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.collections.ArrayList;

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
