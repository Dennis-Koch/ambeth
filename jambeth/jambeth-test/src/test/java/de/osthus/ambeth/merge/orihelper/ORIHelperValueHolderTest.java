package de.osthus.ambeth.merge.orihelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.persistence.xml.model.Address;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("/de/osthus/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/de/osthus/ambeth/persistence/xml/Relations_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm.xml")
public class ORIHelperValueHolderTest extends AbstractPersistenceTest
{
	protected ICache cache;

	protected IObjRefHelper oriHelper;

	protected IProxyHelper proxyHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(oriHelper, "oriHelper");
		ParamChecker.assertNotNull(proxyHelper, "proxyHelper");
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setOriHelper(IObjRefHelper oriHelper)
	{
		this.oriHelper = oriHelper;
	}

	public void setProxyHelper(IProxyHelper proxyHelper)
	{
		this.proxyHelper = proxyHelper;
	}

	@Test
	public void testExtractOrisFromListOfValueHolders() throws Throwable
	{
		Employee employee1 = cache.getObject(Employee.class, 1);
		Employee employee2 = cache.getObject(Employee.class, 2);

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(Employee.class);
		IRelationInfoItem primaryAddressMember = (IRelationInfoItem) metaData.getMemberByName("PrimaryAddress");

		assertTrue(employee1 instanceof IValueHolderContainer);
		assertTrue(employee2 instanceof IValueHolderContainer);
		assertTrue(!proxyHelper.isInitialized(employee1, primaryAddressMember));
		assertTrue(!proxyHelper.isInitialized(employee2, primaryAddressMember));

		IList<IObjRef> extractedORIList = new ArrayList<IObjRef>();
		extractedORIList.addAll(proxyHelper.getObjRefs(employee1, primaryAddressMember));
		extractedORIList.addAll(proxyHelper.getObjRefs(employee2, primaryAddressMember));

		assertEquals(2, extractedORIList.size());

		Address address1 = employee1.getPrimaryAddress();
		Address address2 = employee2.getPrimaryAddress();

		IObjRef ori1 = extractedORIList.get(0);
		assertEquals(Address.class, ori1.getRealType());
		assertEquals(address1.getId(), ori1.getId());
		assertEquals(ObjRef.PRIMARY_KEY_INDEX, ori1.getIdNameIndex());
		assertEquals(address1.getVersion(), ori1.getVersion());

		IObjRef ori2 = extractedORIList.get(1);
		assertEquals(Address.class, ori2.getRealType());
		assertEquals(address2.getId(), ori2.getId());
		assertEquals(ObjRef.PRIMARY_KEY_INDEX, ori2.getIdNameIndex());
		assertEquals(address2.getVersion(), ori2.getVersion());
	}

}
