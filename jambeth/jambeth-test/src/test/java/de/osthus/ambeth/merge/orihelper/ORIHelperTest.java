package de.osthus.ambeth.merge.orihelper;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.merge.independent.EntityB;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.ParamChecker;

@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/merge/orihelper/ORIHelperTest-orm.xml") })
@SQLStructure("ORIHelperTest_structure.sql")
@TestModule({ ORIHelperTestModule.class })
public class ORIHelperTest extends AbstractPersistenceTest
{
	private ORIHelperTestService oriHelperTestService;

	private ICache cache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cache, "cache");
	}

	public void setOriHelperTestService(ORIHelperTestService oriHelperTestService)
	{
		this.oriHelperTestService = oriHelperTestService;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	@Test
	public void testGetCreateORI()
	{
		List<IObjRef> oris = new ArrayList<IObjRef>();

		oris.add(new ObjRef(EntityB.class, 1, 1));

		oriHelperTestService.getAllEntityBs();
		IList<Object> objects = cache.getObjects(oris, CacheDirective.returnMisses());

		IList<Object> objects2 = cache.getObjects(oris, CacheDirective.none());

		Assert.assertEquals(0, objects2.size());
		Assert.assertEquals(oris.size(), objects.size());
	}

}
