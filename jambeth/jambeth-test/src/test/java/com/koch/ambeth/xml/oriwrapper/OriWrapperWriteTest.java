package com.koch.ambeth.xml.oriwrapper;

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

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLDataList;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructureList;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.service.ProcessServiceTestModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.ioc.BootstrapScannerModule;
import com.koch.ambeth.xml.ioc.XmlModule;
import com.koch.ambeth.xml.oriwrapper.OriWrapperTestBed.TestData;

@SQLStructureList({@SQLStructure("../../service/ProcessServiceTest_structure.sql"),
		@SQLStructure("OriWrapper_structure.sql")})
@SQLDataList({@SQLData("../../service/ProcessServiceTest_data.sql"),
		@SQLData("OriWrapper_data.sql")})
@TestPropertiesList({
		@TestProperties(file = "com/koch/ambeth/xml/oriwrapper/OriWrapperTestData.properties"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "com/koch/ambeth/service/orm.xml;com/koch/ambeth/xml/oriwrapper/orm.xml")})
@TestModule({BootstrapScannerModule.class, XmlModule.class, ProcessServiceTestModule.class,
		OriWrapperTestModule.class})
public class OriWrapperWriteTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@Autowired
	protected OriWrapperTestBed oriWrapperTestBed;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		oriWrapperTestBed.init();
	}

	@Test
	public void writeSimpleEntity() {
		TestData testData = oriWrapperTestBed.getSimpleEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeEntityWithRelation() {
		TestData testData = oriWrapperTestBed.getEntityWithRelationTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeMixedArray() {
		TestData testData = oriWrapperTestBed.getMixedArrayTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeMixedList() {
		TestData testData = oriWrapperTestBed.getMixedListTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeMixedLinkedSet() {
		TestData testData = oriWrapperTestBed.getMixedLinkedSetTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeServiceDescription() throws SecurityException, NoSuchMethodException {
		TestData testData = oriWrapperTestBed.getServiceDescriptionTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedEntity() {
		TestData testData = oriWrapperTestBed.getCreatedEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedChildEntity() {
		TestData testData = oriWrapperTestBed.getCreatedChildEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml)) {
			// Compensate for loss of order in set in CUDResut
			testData = oriWrapperTestBed.getCreatedChildEntityTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedParentAndChildEntities() {
		TestData testData = oriWrapperTestBed.getCreatedParentAndChildEntitiesTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml)) {
			// Compensate for loss of order in set
			testData = oriWrapperTestBed.getCreatedParentAndChildEntitiesTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedParentAndChildEntitiesInList() {
		TestData testData = oriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml)) {
			// Compensate for loss of order in set
			testData = oriWrapperTestBed.getCreatedParentAndChildEntitiesInListTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeMultipleCreatedEntities() {
		TestData testData = oriWrapperTestBed.getMultipleCreatedEntitiesTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml)) {
			// Compensate for loss of order in set in CUDResut
			testData = oriWrapperTestBed.getMultipleCreatedEntitiesTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeUpdatedEntity() {
		TestData testData = oriWrapperTestBed.getUpdatedEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeBuidUpdatedEntity() {
		TestData testData = oriWrapperTestBed.getBuidUpdatedEntityTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedAndUpdatedEntities() {
		TestData testData = oriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml)) {
			// Compensate for loss of order in set in CUDResut
			testData = oriWrapperTestBed.getCreatedAndUpdatedEntitiesTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}

	@Test
	public void writeCreatedAndExistingChildren() {
		TestData testData = oriWrapperTestBed.getCreatedAndExistingChildrenTestData();
		String xml = cyclicXmlHandler.write(testData.obj);
		if (!testData.xml.equals(xml)) {
			// Compensate for loss of order in set in CUDResut
			testData = oriWrapperTestBed.getCreatedAndExistingChildrenTestData2();
		}
		Assert.assertEquals(testData.xml, xml);
	}
}
