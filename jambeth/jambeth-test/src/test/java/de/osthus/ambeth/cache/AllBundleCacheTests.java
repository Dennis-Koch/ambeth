package de.osthus.ambeth.cache;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ de.osthus.ambeth.cache.AbstractCacheTest.class, //
		de.osthus.ambeth.cache.bytecode.EntityBytecodeTest.class, //
		de.osthus.ambeth.cache.RootCacheTest.class, //
		de.osthus.ambeth.cache.ChildCacheTest.class, //
		de.osthus.ambeth.cache.cacheretriever.CacheRetrieverRegistryTest.class, //
		de.osthus.ambeth.cache.directobjref.ChildCacheDirectObjRefTest.class })
public class AllBundleCacheTests
{

}
