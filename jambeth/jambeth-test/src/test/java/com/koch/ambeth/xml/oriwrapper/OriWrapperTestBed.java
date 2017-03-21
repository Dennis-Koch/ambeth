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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.service.SyncToAsyncUtil;
import com.koch.ambeth.transfer.ITestService;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.config.IProperties;

public class OriWrapperTestBed implements IDisposableBean
{
	private static final long UTC_2001_01_01_00_00_00 = 978307200000L;

	private static final String TEST_DATA_JAVA = "test.data.OriWrapperTest.%s.java";

	private static final String TEST_DATA_CS = "test.data.OriWrapperTest.%s.cs";

	public static final class TestData
	{
		public final String xml;

		public final String xmlCS;

		public final Object obj;

		public TestData(String xml, Object obj)
		{
			this(xml, xml, obj);
		}

		public TestData(String xml, String xmlCS, Object obj)
		{
			this.xml = xml;
			this.xmlCS = xmlCS.replace("Int16N", "Int16").replace("Int32N", "Int32");
			this.obj = obj;
		}
	}

	@LogInstance
	private ILogger log;

	protected IDisposableCache cache;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IProperties properties;

	@Override
	public void destroy() throws Throwable
	{
		if (cache != null)
		{
			cache.dispose();
			cache = null;
		}
	}

	public void init()
	{
		// Test entities have to come from an independent cache
		if (cache != null)
		{
			cache.dispose();
			cache = null;
		}
		cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");
	}

	public TestData getSimpleEntityTestData()
	{
		String dataName = "SimpleEntity";
		String xml = getJavaTestXml(dataName);
		Object obj = cache.getObject(MaterialGroup.class, "1");
		return new TestData(xml, obj);
	}

	public TestData getEntityWithRelationTestData()
	{
		String dataName = "EntityWithRelation";
		String xml = getJavaTestXml(dataName);
		Object obj = cache.getObject(Material.class, 1);
		return new TestData(xml, obj);
	}

	public TestData getMixedArrayTestData()
	{
		String dataName = "MixedArray";
		String xml = getJavaTestXml(dataName);
		Object obj = getColletionData();
		return new TestData(xml, obj);
	}

	public TestData getMixedListTestData()
	{
		String dataName = "MixedList";
		String xml = getJavaTestXml(dataName);
		Object obj = Arrays.asList(getColletionData());
		return new TestData(xml, obj);
	}

	public TestData getMixedLinkedSetTestData()
	{
		// Sets loose the duplicate elements
		String dataName = "MixedLinkedSet";
		String xml = getJavaTestXml(dataName);
		Object obj = new LinkedHashSet<Object>(Arrays.asList(getColletionData()));
		return new TestData(xml, obj);
	}

	public TestData getServiceDescriptionTestData() throws SecurityException, NoSuchMethodException
	{
		String dataName = "ServiceDescription";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);

		Class<?>[] argTypes = { int.class, Material.class, String.class, MaterialGroup.class, Material.class, Date.class };
		Method serviceMethod = ITestService.class.getMethod("mixedParamsNoReturn", argTypes);
		Object obj = SyncToAsyncUtil.createServiceDescription("TestService", serviceMethod, getColletionData());
		return new TestData(xml, xmlCS, obj);
	}

	public TestData getCreatedEntityTestData()
	{
		String dataName = "CreatedEntity";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);

		MaterialGroup materialGroup = cache.getObject(MaterialGroup.class, "1");
		Material obj = entityFactory.createEntity(Material.class);
		obj.setBuid("Created Material");
		obj.setMaterialGroup(materialGroup);
		obj.setName("Created Material");

		return new TestData(xml, xmlCS, obj);
	}

	public TestData getCreatedChildEntityTestData()
	{
		String dataName = "CreatedChildEntity";
		String xml = getJavaTestXml(dataName);
		String xmlCS = xml.replace("\"Object\"", "\"com.koch.ambeth.merge.model.IChangeContainer\"");

		MaterialGroup materialGroup = entityFactory.createEntity(MaterialGroup.class);
		materialGroup.setBuid("new mg");
		materialGroup.setName("new mg name");

		Material obj = cache.getObject(Material.class, 1);
		obj.setMaterialGroup(materialGroup);

		return new TestData(xml, xmlCS, obj);
	}

	public TestData getCreatedChildEntityTestData2()
	{
		String dataName = "CreatedChildEntity2";
		String xml = getJavaTestXml(dataName);
		String xmlCS = xml.replace("\"Object\"", "\"com.koch.ambeth.merge.model.IChangeContainer\"");
		TestData testData = getCreatedChildEntityTestData();
		return new TestData(xml, xmlCS, testData.obj);
	}

	public TestData getCreatedParentAndChildEntitiesTestData()
	{
		String dataName = "CreatedParentAndChildEntities";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);

		MaterialGroup materialGroup = entityFactory.createEntity(MaterialGroup.class);
		materialGroup.setBuid("new mg");
		materialGroup.setName("new mg name");

		Material obj = entityFactory.createEntity(Material.class);
		obj.setBuid("Created Material");
		obj.setMaterialGroup(materialGroup);
		obj.setName("Created Material");
		return new TestData(xml, xmlCS, obj);
	}

	public TestData getCreatedParentAndChildEntitiesTestData2()
	{
		String dataName = "CreatedParentAndChildEntities2";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);
		TestData testData = getCreatedParentAndChildEntitiesTestData();
		return new TestData(xml, xmlCS, testData.obj);
	}

	public TestData getCreatedParentAndChildEntitiesInListTestData()
	{
		String dataName = "CreatedParentAndChildEntitiesInList";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);

		MaterialGroup materialGroup = entityFactory.createEntity(MaterialGroup.class);
		materialGroup.setBuid("new mg");
		materialGroup.setName("new mg name");

		Material newMaterial = entityFactory.createEntity(Material.class);
		newMaterial.setBuid("Created Material");
		newMaterial.setMaterialGroup(materialGroup);
		newMaterial.setName("Created Material");

		List<Object> obj = Arrays.asList(newMaterial, materialGroup);

		return new TestData(xml, xmlCS, obj);
	}

	public TestData getCreatedParentAndChildEntitiesInListTestData2()
	{
		String dataName = "CreatedParentAndChildEntitiesInList2";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);
		TestData testData = getCreatedParentAndChildEntitiesInListTestData();
		return new TestData(xml, xmlCS, testData.obj);
	}

	public TestData getMultipleCreatedEntitiesTestData()
	{
		String dataName = "MultipleCreatedEntities";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);

		MaterialGroup materialGroup = cache.getObject(MaterialGroup.class, "1");

		Material newMaterial1 = entityFactory.createEntity(Material.class);
		newMaterial1.setBuid("Created Material");
		newMaterial1.setMaterialGroup(materialGroup);
		newMaterial1.setName("Created Material");

		Material newMaterial2 = entityFactory.createEntity(Material.class);
		newMaterial2.setBuid("Created Material 2");
		newMaterial2.setMaterialGroup(materialGroup);
		newMaterial2.setName("Created Material 2");

		Object obj = Arrays.asList(newMaterial1, newMaterial2);

		return new TestData(xml, xmlCS, obj);
	}

	public TestData getMultipleCreatedEntitiesTestData2()
	{
		String dataName = "MultipleCreatedEntities2";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);
		TestData testData = getMultipleCreatedEntitiesTestData();
		return new TestData(xml, xmlCS, testData.obj);
	}

	public TestData getCreatedAndExistingChildrenTestData()
	{
		String dataName = "CreatedAndExistingChildren";
		String xml = getJavaTestXml(dataName);
		String xmlCS = xml.replace("\"Object\"", "\"com.koch.ambeth.merge.model.IChangeContainer\"");

		EntityA entityA = cache.getObject(EntityA.class, 1);
		EntityB entityB = entityFactory.createEntity(EntityB.class);
		entityA.getEntityBs().add(entityB);

		return new TestData(xml, xmlCS, entityA);
	}

	public TestData getCreatedAndExistingChildrenTestData2()
	{
		String dataName = "CreatedAndExistingChildren2";
		String xml = getJavaTestXml(dataName);
		TestData testData = getCreatedAndExistingChildrenTestData();
		return new TestData(xml, testData.obj);
	}

	public TestData getUpdatedEntityTestData()
	{
		String dataName = "UpdatedEntity";
		String xml = getJavaTestXml(dataName);
		String xmlCS = xml.replace("\"Object\"", "\"com.koch.ambeth.merge.model.IChangeContainer\"");
		Material obj = cache.getObject(Material.class, 1);
		obj.setName(obj.getName() + ".2");
		return new TestData(xml, xmlCS, obj);
	}

	public TestData getBuidUpdatedEntityTestData()
	{
		String dataName = "BuidUpdatedEntity";
		String xml = getJavaTestXml(dataName);
		Material obj = cache.getObject(Material.class, 1);
		obj.setBuid(obj.getBuid() + ".2");
		return new TestData(xml, obj);
	}

	public TestData getCreatedAndUpdatedEntitiesTestData()
	{
		String dataName = "CreatedAndUpdatedEntities";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);

		Material material = cache.getObject(Material.class, 1);
		material.setName("Material 1.2");

		Material newMaterial = entityFactory.createEntity(Material.class);
		newMaterial.setBuid("Created Material");
		newMaterial.setMaterialGroup(material.getMaterialGroup());
		newMaterial.setName("Created Material");

		Object obj = Arrays.asList(material, newMaterial);

		return new TestData(xml, xmlCS, obj);
	}

	public TestData getCreatedAndUpdatedEntitiesTestData2()
	{
		String dataName = "CreatedAndUpdatedEntities2";
		String xml = getJavaTestXml(dataName);
		String xmlCS = getCsTestXml(dataName);
		TestData testData = getCreatedAndUpdatedEntitiesTestData();
		return new TestData(xml, xmlCS, testData.obj);
	}

	protected Object[] getColletionData()
	{
		Material material = cache.getObject(Material.class, 1);
		MaterialGroup materialGroup = cache.getObject(MaterialGroup.class, "1");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(UTC_2001_01_01_00_00_00);
		return new Object[] { 3, material, "test", materialGroup, material, cal.getTime() };
	}

	protected String getJavaTestXml(String dataName)
	{
		return properties.getString(String.format(TEST_DATA_JAVA, dataName)).trim();
	}

	protected String getCsTestXml(String dataName)
	{
		return properties.getString(String.format(TEST_DATA_CS, dataName)).trim();
	}
}
