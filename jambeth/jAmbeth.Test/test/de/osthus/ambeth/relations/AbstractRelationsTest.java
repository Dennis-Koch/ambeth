package de.osthus.ambeth.relations;

import junit.framework.Assert;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.util.ParamChecker;

@TestModule(RelationsTestModule.class)
public abstract class AbstractRelationsTest extends AbstractPersistenceTest
{
	protected ICache cache;

	protected IProxyHelper proxyHelper;

	protected IRelationsService relationsService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(proxyHelper, "ProxyHelper");
		ParamChecker.assertNotNull(relationsService, "relationsService");
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setProxyHelper(IProxyHelper proxyHelper)
	{
		this.proxyHelper = proxyHelper;
	}

	public void setRelationsService(IRelationsService relationsService)
	{
		this.relationsService = relationsService;
	}

	protected void assertBeforePrefetch(Object entity, String propertyName)
	{
		Assert.assertTrue(Boolean.FALSE.equals(proxyHelper.isInitialized(entity, propertyName)));
	}

	protected void assertAfterPrefetch(Object entity, String propertyName)
	{
		Assert.assertTrue(Boolean.TRUE.equals(proxyHelper.isInitialized(entity, propertyName)));
		Assert.assertNull(proxyHelper.getObjRefs(entity, propertyName));
	}
}
