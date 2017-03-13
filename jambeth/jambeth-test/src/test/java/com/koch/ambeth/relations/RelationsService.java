package com.koch.ambeth.relations;

import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.service.proxy.Service;

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
