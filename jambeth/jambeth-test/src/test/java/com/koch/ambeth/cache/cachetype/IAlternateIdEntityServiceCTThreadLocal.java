package com.koch.ambeth.cache.cachetype;

import com.koch.ambeth.cache.CacheContext;
import com.koch.ambeth.cache.CacheType;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.jdbc.alternateid.IAlternateIdEntityService;
import com.koch.ambeth.service.proxy.Service;

@Service(IAlternateIdEntityServiceCTThreadLocal.class)
@PersistenceContext
@CacheContext(CacheType.THREAD_LOCAL)
public interface IAlternateIdEntityServiceCTThreadLocal extends IAlternateIdEntityService
{
	// Intended blank
}
