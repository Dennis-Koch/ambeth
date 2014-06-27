package de.osthus.ambeth.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.ioc.DataChangePersistenceModule;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.DataChangeEntryBO;
import de.osthus.ambeth.model.DataChangeEventBO;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

@SQLStructure("/DataChangeEvent_structure.sql")
@SQLData("DataChangeEvent_data.sql")
@TestModule(DataChangePersistenceModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "datachange-orm.xml")
public class DataChangeEventServiceTest extends AbstractPersistenceTest
{
	private IDataChangeEventService fixture;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(fixture, "fixture");
	}

	public void setFixture(IDataChangeEventService fixture)
	{
		this.fixture = fixture;
	}

	@Test
	public void testSave()
	{
		DataChangeEvent dce1 = new DataChangeEvent();
		DataChangeEntry dcEnt = new DataChangeEntry();
		dcEnt.setEntityType(DataChangeEventBO.class);
		dcEnt.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
		dcEnt.setId(3);
		dcEnt.setVersion(2);
		dce1.setUpdates(Collections.<IDataChangeEntry> singletonList(dcEnt));

		fixture.save(dce1);

		DataChangeEvent dce2 = new DataChangeEvent();

		fixture.save(dce2);
	}

	@Test
	public void testRetrieveAll1() throws Throwable
	{
		EventStoreDummy dummy = new EventStoreDummy();
		ReflectUtil.getDeclaredField(fixture.getClass(), "eventStore").set(fixture, dummy);
		((DataChangeEventService) fixture).afterStarted();
		List<Object> all = dummy.getEventObjects();
		assertNotNull(all);
		assertEquals(1, all.size());

		IDataChange entity = (IDataChange) all.get(0);
		assertEquals(123456789, entity.getChangeTime());
		assertNotNull(entity.getInserts());
		assertTrue(entity.getInserts().isEmpty());
		assertNotNull(entity.getUpdates());
		assertFalse(entity.getUpdates().isEmpty());
		assertNotNull(entity.getDeletes());
		assertTrue(entity.getDeletes().isEmpty());

		List<IDataChangeEntry> updates = entity.getUpdates();
		assertEquals(1, updates.size());

		IDataChangeEntry dcEnt = updates.get(0);
		assertEquals(DataChangeEntryBO.class, dcEnt.getEntityType());
		assertEquals(-1, dcEnt.getIdNameIndex());
		assertEquals(Long.valueOf(2), dcEnt.getId());
		assertEquals(Byte.valueOf((byte) 21), dcEnt.getVersion());
	}

	@Test
	public void testRetrieveAll2() throws Throwable
	{
		EventStoreDummy dummy = new EventStoreDummy();
		ReflectUtil.getDeclaredField(fixture.getClass(), "eventStore").set(fixture, dummy);
		((DataChangeEventService) fixture).afterStarted();
		List<Object> all = dummy.getEventObjects();
		assertNotNull(all);
		assertEquals(1, all.size());

		DataChangeEvent dce1 = new DataChangeEvent();
		DataChangeEntry dcEnt = new DataChangeEntry();
		dcEnt.setEntityType(DataChangeEventBO.class);
		dcEnt.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
		dcEnt.setId(3);
		dcEnt.setVersion(2);
		dce1.setUpdates(Collections.<IDataChangeEntry> singletonList(dcEnt));

		fixture.save(dce1);
		((DataChangeEventService) fixture).afterStarted();
		all = dummy.getEventObjects();
		assertNotNull(all);
		assertEquals(2, all.size());

		DataChangeEvent dce2 = new DataChangeEvent();

		fixture.save(dce2);
		((DataChangeEventService) fixture).afterStarted();
		all = dummy.getEventObjects();
		assertNotNull(all);
		assertEquals(3, all.size());
	}
}
