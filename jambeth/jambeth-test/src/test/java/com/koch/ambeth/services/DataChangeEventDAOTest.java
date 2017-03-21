package com.koch.ambeth.services;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.koch.ambeth.datachange.persistence.ioc.DataChangePersistenceModule;
import com.koch.ambeth.datachange.persistence.model.DataChangeEntryBO;
import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.datachange.persistence.model.EntityType;
import com.koch.ambeth.datachange.persistence.services.IDataChangeEventDAO;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;

@SQLStructure("DataChangeEvent_structure.sql")
@SQLData("DataChangeEvent_data.sql")
@TestModule(DataChangePersistenceModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "datachange-orm.xml")
public class DataChangeEventDAOTest extends AbstractInformationBusWithPersistenceTest
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
