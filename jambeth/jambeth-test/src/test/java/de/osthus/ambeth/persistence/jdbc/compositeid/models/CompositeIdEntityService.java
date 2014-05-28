package de.osthus.ambeth.persistence.jdbc.compositeid.models;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.Service;

@Service(ICompositeIdEntityService.class)
public class CompositeIdEntityService implements ICompositeIdEntityService
{
	@SuppressWarnings("unused")
	@LogInstance(CompositeIdEntityService.class)
	private ILogger log;

	@Override
	public CompositeIdEntity create(CompositeIdEntity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CompositeIdEntity update(CompositeIdEntity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(CompositeIdEntity entity)
	{
		throw new UnsupportedOperationException();
	}
}
