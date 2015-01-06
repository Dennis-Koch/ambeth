package de.osthus.ambeth.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.ioc.DataChangePersistenceModule;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.DataChangeEntryBO;
import de.osthus.ambeth.model.DataChangeEventBO;
import de.osthus.ambeth.model.EntityType;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@SQLStructure("DataChangeEvent_structure.sql")
@SQLData("DataChangeEvent_data.sql")
@TestModule(DataChangePersistenceModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "datachange-orm.xml")
public class DataChangeEventDAOTest extends AbstractPersistenceTest
{
	private IDataChangeEventDAO fixture;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(fixture, "fixture");
	}

	public void setFixture(IDataChangeEventDAO fixture)
	{
		this.fixture = fixture;
	}

	@Test
	public void testSave()
	{
		EntityType etDCE = entityFactory.createEntity(EntityType.class);
		etDCE.setType(DataChangeEvent.class);

		DataChangeEventBO dce1 = entityFactory.createEntity(DataChangeEventBO.class);
		DataChangeEntryBO dcEnt = entityFactory.createEntity(DataChangeEntryBO.class);
		dcEnt.setEntityType(etDCE);
		dcEnt.setIdIndex(ObjRef.PRIMARY_KEY_INDEX);
		dcEnt.setObjectId("3");
		dcEnt.setObjectVersion("2");
		dce1.setUpdates(Collections.singletonList(dcEnt));

		fixture.save(dce1);

		DataChangeEventBO dce2 = entityFactory.createEntity(DataChangeEventBO.class);

		fixture.save(dce2);
	}

	@Test
	public void testRetrieveAll1()
	{
		List<DataChangeEventBO> all = fixture.retrieveAll();
		assertNotNull(all);
		assertEquals(1, all.size());

		DataChangeEventBO bo = all.get(0);
		assertEquals(123456789, bo.getChangeTime());
		assertNotNull(bo.getInserts());
		assertTrue(bo.getInserts().isEmpty());
		assertNotNull(bo.getUpdates());
		assertFalse(bo.getUpdates().isEmpty());
		assertNotNull(bo.getDeletes());
		assertTrue(bo.getDeletes().isEmpty());

		List<DataChangeEntryBO> updates = bo.getUpdates();
		assertEquals(1, updates.size());

		DataChangeEntryBO dcEnt = updates.get(0);
		assertEquals(DataChangeEntryBO.class, dcEnt.getEntityType().getType());
		assertEquals(-1, dcEnt.getIdIndex());
		assertEquals("2", dcEnt.getObjectId());
		assertEquals("21", dcEnt.getObjectVersion());
	}

	@Test
	public void testRetrieveAll2()
	{
		List<DataChangeEventBO> all = fixture.retrieveAll();
		assertNotNull(all);
		assertEquals(1, all.size());

		EntityType etDCE = entityFactory.createEntity(EntityType.class);
		etDCE.setType(DataChangeEvent.class);

		DataChangeEventBO dce1 = entityFactory.createEntity(DataChangeEventBO.class);
		DataChangeEntryBO dcEnt = entityFactory.createEntity(DataChangeEntryBO.class);
		dcEnt.setEntityType(etDCE);
		dcEnt.setIdIndex(ObjRef.PRIMARY_KEY_INDEX);
		dcEnt.setObjectId("3");
		dcEnt.setObjectVersion("2");
		dce1.setUpdates(Collections.singletonList(dcEnt));

		fixture.save(dce1);
		all = fixture.retrieveAll();
		assertNotNull(all);
		assertEquals(2, all.size());

		DataChangeEventBO dce2 = entityFactory.createEntity(DataChangeEventBO.class);

		fixture.save(dce2);
		all = fixture.retrieveAll();
		assertNotNull(all);
		assertEquals(3, all.size());
	}

	@Test
	public void testRemoveBefore()
	{
		List<DataChangeEventBO> all = fixture.retrieveAll();
		assertNotNull(all);
		assertEquals(1, all.size());

		fixture.removeBefore(123456790);
		all = fixture.retrieveAll();
		assertNotNull(all);
		assertEquals(0, all.size());
	}
}
