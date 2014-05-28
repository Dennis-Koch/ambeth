package de.osthus.ambeth.persistence.jdbc.splitloading;

import java.util.List;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.Service;

@Service(IRefEntityService.class)
public class RefEntityService implements IRefEntityService
{
	@SuppressWarnings("unused")
	@LogInstance(RefEntityService.class)
	private ILogger log;

	@Override
	public void save(List<RefEntity> entities)
	{
		throw new UnsupportedOperationException();
	}
}
