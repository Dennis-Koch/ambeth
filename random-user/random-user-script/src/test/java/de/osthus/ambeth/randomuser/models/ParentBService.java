package de.osthus.ambeth.randomuser.models;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.Service;

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
