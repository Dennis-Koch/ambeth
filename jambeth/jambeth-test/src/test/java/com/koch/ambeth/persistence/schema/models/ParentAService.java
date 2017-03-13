package com.koch.ambeth.persistence.schema.models;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.proxy.Service;

@Service(IParentAService.class)
public class ParentAService implements IParentAService
{
	@SuppressWarnings("unused")
	@LogInstance(ParentAService.class)
	private ILogger log;

	@Override
	public ParentA create(ParentA entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ParentA retrieve(int id)
	{
		return null;
	}

	@Override
	public ParentA update(ParentA entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(ParentA entity)
	{
		throw new UnsupportedOperationException();
	}
}
