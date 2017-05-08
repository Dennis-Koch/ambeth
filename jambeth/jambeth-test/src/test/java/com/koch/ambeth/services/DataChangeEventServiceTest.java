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

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.persistence.ioc.DataChangePersistenceModule;
import com.koch.ambeth.datachange.persistence.model.DataChangeEntryBO;
import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.datachange.persistence.services.DataChangeEventService;
import com.koch.ambeth.datachange.persistence.services.IDataChangeEventService;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;

@SQLStructure("DataChangeEvent_structure.sql")
@SQLData("DataChangeEvent_data.sql")
@TestModule(DataChangePersistenceModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "datachange-orm.xml")
public class DataChangeEventServiceTest extends AbstractInformationBusWithPersistenceTest {
	private IDataChangeEventService fixture;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(fixture, "fixture");
	}

	public void setFixture(IDataChangeEventService fixture) {
		this.fixture = fixture;
	}

	@Test
	public void testSave() {
		DataChangeEvent dce1 = new DataChangeEvent();
		DataChangeEntry dcEnt = new DataChangeEntry();
		dcEnt.setEntityType(DataChangeEventBO.class);
		dcEnt.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
		dcEnt.setId(3);
		dcEnt.setVersion(2);
		dce1.setUpdates(Collections.<IDataChangeEntry>singletonList(dcEnt));

		fixture.save(dce1);

		DataChangeEvent dce2 = new DataChangeEvent();

		fixture.save(dce2);
	}

	@Test
	public void testRetrieveAll1() throws Throwable {
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
	public void testRetrieveAll2() throws Throwable {
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
		dce1.setUpdates(Collections.<IDataChangeEntry>singletonList(dcEnt));

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
