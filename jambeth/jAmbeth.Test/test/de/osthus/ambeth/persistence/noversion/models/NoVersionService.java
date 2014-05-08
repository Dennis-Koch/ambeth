package de.osthus.ambeth.persistence.noversion.models;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.Service;

@Service(INoVersionService.class)
public class NoVersionService implements INoVersionService
{
	@SuppressWarnings("unused")
	@LogInstance(NoVersionService.class)
	private ILogger log;

	@Override
	public NoVersion create(NoVersion entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public NoVersion update(NoVersion entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(NoVersion entity)
	{
		throw new UnsupportedOperationException();
	}
}
