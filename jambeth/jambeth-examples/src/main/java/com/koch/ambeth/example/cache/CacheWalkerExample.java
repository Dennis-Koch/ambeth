package com.koch.ambeth.example.cache;

import com.koch.ambeth.cache.walker.ICacheWalker;
import com.koch.ambeth.cache.walker.ICacheWalkerResult;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.service.merge.model.IObjRef;

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
