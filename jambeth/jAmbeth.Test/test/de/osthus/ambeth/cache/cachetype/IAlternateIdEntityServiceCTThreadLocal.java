package de.osthus.ambeth.cache.cachetype;

import de.osthus.ambeth.cache.CacheContext;
import de.osthus.ambeth.cache.CacheType;
import de.osthus.ambeth.persistence.jdbc.alternateid.IAlternateIdEntityService;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;

@Service(IAlternateIdEntityServiceCTThreadLocal.class)
@PersistenceContext
@CacheContext(CacheType.THREAD_LOCAL)
public interface IAlternateIdEntityServiceCTThreadLocal extends IAlternateIdEntityService
{
	// Intended blank
}
