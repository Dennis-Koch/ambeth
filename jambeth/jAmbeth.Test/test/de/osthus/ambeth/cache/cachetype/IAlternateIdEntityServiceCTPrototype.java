package de.osthus.ambeth.cache.cachetype;

import de.osthus.ambeth.cache.CacheContext;
import de.osthus.ambeth.cache.CacheType;
import de.osthus.ambeth.persistence.jdbc.alternateid.IAlternateIdEntityService;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.Service;

@Service(IAlternateIdEntityServiceCTPrototype.class)
@PersistenceContext
@CacheContext(CacheType.PROTOTYPE)
public interface IAlternateIdEntityServiceCTPrototype extends IAlternateIdEntityService
{
	// Intended blank
}
