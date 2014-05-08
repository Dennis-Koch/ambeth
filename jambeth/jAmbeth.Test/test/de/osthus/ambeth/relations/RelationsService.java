package de.osthus.ambeth.relations;

import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;

@Service(IRelationsService.class)
@PersistenceContext
public class RelationsService implements IRelationsService
{
	@Override
	public void save(Object obj)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Object obj)
	{
		throw new UnsupportedOperationException();
	}
}
