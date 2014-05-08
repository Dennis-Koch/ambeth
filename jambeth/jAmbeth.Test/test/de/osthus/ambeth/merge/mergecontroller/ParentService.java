package de.osthus.ambeth.merge.mergecontroller;

import javax.persistence.PersistenceContext;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.Service;

@PersistenceContext
@Service(IParentService.class)
public class ParentService implements IParentService
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void save(Parent parent)
	{
		throw new IllegalArgumentException();
	}
}
