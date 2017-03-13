package com.koch.ambeth.persistence.jdbc.splitloading;

import java.util.List;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.proxy.Service;

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
