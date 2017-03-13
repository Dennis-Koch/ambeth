package com.koch.ambeth.persistence.schema.models;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.proxy.Service;

@Service(IParentBService.class)
public class ParentBService implements IParentBService
{
	@SuppressWarnings("unused")
	@LogInstance(ParentBService.class)
	private ILogger log;

	@Override
	public ParentB create(ParentB entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ParentB retrieve(int id)
	{
		return null;
	}

	@Override
	public ParentB update(ParentB entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(ParentB entity)
	{
		throw new UnsupportedOperationException();
	}
}
