package de.osthus.ambeth.metadata;

import org.junit.Test;

import de.osthus.ambeth.cache.valueholdercontainer.Material;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.objrefstore.IObjRefStoreEntryProvider;
import de.osthus.ambeth.objrefstore.ObjRefStore;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/cache/valueholdercontainer/orm.xml")
@TestFrameworkModule(ObjRefFactoryTestModule.class)
public class ObjRefFactoryTest extends AbstractInformationBusTest
{
	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IObjRefStoreEntryProvider objRefStoreEntryProvider;

	@Test
	public void performance()
	{
		int count = 2000000000;

		for (int a = 10; a-- > 0;)
		{
			doLoopFactory(100);
		}
		doLoopFactory(count);
		doLoopFactory(count);

		for (int a = 10; a-- > 0;)
		{
			doLoopConstructor(100);
		}
		doLoopConstructor(count);
		doLoopConstructor(count);
	}

	protected void doLoopFactory(int count)
	{
		long start = System.currentTimeMillis();
		Integer one = Integer.valueOf(1);
		Short two = Short.valueOf((short) 2);
		for (int a = count; a-- > 0;)
		{
			ObjRefStore objRefStore = objRefStoreEntryProvider.createObjRefStore(Material.class, (byte) -1, one);
			objRefStore.setVersion(two);
			IObjRef objRef = objRefFactory.createObjRef(Material.class, -1, one, two);
			System.out.println(objRef);
		}
		System.out.println((System.currentTimeMillis() - start) + " ms Factory");
	}

	protected void doLoopConstructor(int count)
	{
		long start = System.currentTimeMillis();
		Integer one = Integer.valueOf(1);
		Short two = Short.valueOf((short) 2);
		for (int a = count; a-- > 0;)
		{
			new ObjRef(Material.class, (byte) 1, one, two);
		}
		System.out.println((System.currentTimeMillis() - start) + " ms Constructor");
	}
}
