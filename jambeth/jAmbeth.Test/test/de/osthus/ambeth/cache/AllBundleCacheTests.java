package de.osthus.ambeth.cache;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.cache.bytecode.EntityBytecodeTest;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.cache.AbstractCacheTest.class, EntityBytecodeTest.class, de.osthus.ambeth.cache.RootCacheTest.class,
		de.osthus.ambeth.cache.ChildCacheTest.class, de.osthus.ambeth.cache.cacheretriever.CacheRetrieverRegistryTest.class })
public class AllBundleCacheTests
{

}
