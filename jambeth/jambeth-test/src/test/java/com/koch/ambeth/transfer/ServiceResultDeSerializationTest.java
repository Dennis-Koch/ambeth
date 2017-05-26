package com.koch.ambeth.transfer;

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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.service.ProcessServiceTestModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.ioc.BootstrapScannerModule;
import com.koch.ambeth.xml.ioc.XmlModule;

@SQLStructure("../service/ProcessServiceTest_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/service/orm.xml")
@TestModule({BootstrapScannerModule.class, XmlModule.class, ProcessServiceTestModule.class})
public class ServiceResultDeSerializationTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	protected ICache cache;

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXMLHandler;

	@Test
	public void testNoReturn() throws Exception {
		runTest(null);
	}

	@Test
	public void testPrimitiveReturn() throws Exception {
		runTest(2);
	}

	@Test
	public void testPrimitiveArrayReturn() throws Exception {
		int[] returnValue = new int[] {1, 23, 4};
		int[] actual = (int[]) runTest(returnValue);
		assertArrayEquals(returnValue, actual);
	}

	@Test
	public void testPrimitiveListReturn() throws Exception {
		Object returnValue = Arrays.asList(1, 2, 3, 4);
		runTest(returnValue);
	}

	@Test
	public void testDateReturn() throws Exception {
		Object returnValue = new Date();
		runTest(returnValue);
	}

	@Test
	@SQLData("../service/ProcessServiceTest_data.sql")
	public void testEntityReturn() throws Exception {
		MaterialGroup returnValue = cache.getObject(MaterialGroup.class, "1");
		runTest(returnValue);
	}

	@Test
	public void testEntityRelationReturn() throws Exception {
		Material returnValue = cache.getObject(Material.class, 1);
		runTest(returnValue);
	}

	private Object runTest(Object returnValue) throws NoSuchMethodException {
		String xml = cyclicXMLHandler.write(returnValue);
		Object actual = cyclicXMLHandler.read(xml);
		if (actual == null || !actual.getClass().isArray()) {
			assertEquals(returnValue, actual);
		}
		return actual;
	}
}
