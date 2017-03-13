package com.koch.ambeth.merge.mergecontroller;

import javax.persistence.PersistenceContext;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.proxy.Service;

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
