package com.koch.ambeth.cache;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ com.koch.ambeth.cache.AbstractCacheTest.class, //
		com.koch.ambeth.cache.bytecode.EntityBytecodeTest.class, //
		com.koch.ambeth.cache.RootCacheTest.class, //
		com.koch.ambeth.cache.ChildCacheTest.class, //
		com.koch.ambeth.cache.cacheretriever.CacheRetrieverRegistryTest.class, //
		com.koch.ambeth.cache.directobjref.ChildCacheDirectObjRefTest.class, //
		com.koch.ambeth.cache.valueholdercontainer.ValueHolderContainerTest.class })
public class AllBundleCacheTests
{

}
