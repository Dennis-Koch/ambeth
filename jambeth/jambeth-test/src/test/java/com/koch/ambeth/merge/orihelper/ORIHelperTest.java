package com.koch.ambeth.merge.orihelper;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.independent.EntityB;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/merge/orihelper/ORIHelperTest-orm.xml") })
@SQLStructure("ORIHelperTest_structure.sql")
@TestModule({ ORIHelperTestModule.class })
public class ORIHelperTest extends AbstractInformationBusWithPersistenceTest
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
