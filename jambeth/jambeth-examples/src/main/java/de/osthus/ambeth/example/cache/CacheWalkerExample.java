package de.osthus.ambeth.example.cache;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.walker.ICacheWalker;
import de.osthus.ambeth.cache.walker.ICacheWalkerResult;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;

public class CacheWalkerExample {
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheWalker cacheWalker;

	@Autowired
	protected ICache cache;

	public Object loadEntity(IObjRef objRef) {
		Object entity = cache.getObject(objRef, CacheDirective.none());
		ICacheWalkerResult walkerResult = cacheWalker.walk(objRef);
		if (log.isInfoEnabled()) {
			log.info(walkerResult.toString());
		}
		return entity;
	}
}
