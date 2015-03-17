using System;
using System.Collections.Generic;
using System.Reflection;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Test.Model;
using De.Osthus.Ambeth.Xml.Test.Transfer;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class OriWrapperTestBed : IDisposableBean
    {
        private const String TEST_DATA_JAVA = "test.data.OriWrapperTest.{0}.java";

        private const String TEST_DATA_CS = "test.data.OriWrapperTest.{0}.cs";

        public class TestData
        {
            public String xml;

            public String xmlJava;

            public Object obj;

            public TestData(String xml, Object obj)
                : this(xml, xml, obj)
            {
            }

            public TestData(String xml, String xmlCS, Object obj)
            {
                this.xmlJava = xml;
                this.xml = xmlCS.Replace("Int16N", "Int16").Replace("Int32N", "Int32");
                this.obj = obj;
            }
        }

        protected IDisposableCache cache;

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IProperties Properties { protected get; set; }

        public void Destroy()
        {
            if (cache != null)
            {
                cache.Dispose();
                cache = null;
            }
        }

        public void Init()
        {
            // Test entities have to come from an independent cache
            if (cache != null)
            {
                cache.Dispose();
                cache = null;
            }
            cache = CacheFactory.Create(CacheFactoryDirective.NoDCE, "test");
        }

        public TestData getSimpleEntityTestData()
        {
            String dataName = "SimpleEntity";
            String xml = GetJavaTestXml(dataName);
            Object obj = cache.GetObject(typeof(MaterialGroup), "1");
            return new TestData(xml, obj);
        }

        public TestData getEntityWithRelationTestData()
        {
            String dataName = "EntityWithRelation";
            String xml = GetJavaTestXml(dataName);
            Object obj = cache.GetObject<Material>(1);
            return new TestData(xml, obj);
        }

        public TestData getMixedArrayTestData()
        {
            String dataName = "MixedArray";
            String xml = GetJavaTestXml(dataName);
            Object obj = ColletionData;
            return new TestData(xml, obj);
        }

        public TestData getMixedListTestData()
        {
            String dataName = "MixedList";
            String xml = GetJavaTestXml(dataName);
            Object obj = new List<Object>(ColletionData);
            return new TestData(xml, obj);
        }

        public TestData getMixedLinkedSetTestData()
        {
            // Sets loose the duplicate elements
            String dataName = "MixedLinkedSet";
            String xml = GetJavaTestXml(dataName);
            Object obj = new LinkedHashSet<Object>(ColletionData);
            return new TestData(xml, obj);
        }

        public TestData getServiceDescriptionTestData()
        {
            String dataName = "ServiceDescription";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);

            Type[] argTypes = { typeof(int), typeof(Material), typeof(String), typeof(MaterialGroup), typeof(Material), typeof(DateTime) };
            MethodInfo serviceMethod = typeof(ITestService).GetMethod("MixedParamsNoReturn", argTypes);
            Object obj = SyncToAsyncUtil.CreateServiceDescription("TestService", serviceMethod, ColletionData);
            return new TestData(xml, xmlCS, obj);
        }

        public TestData getCreatedEntityTestData()
        {
            String dataName = "CreatedEntity";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);

            MaterialGroup materialGroup = cache.GetObject<MaterialGroup>("1");

            Material obj = EntityFactory.CreateEntity<Material>();
            obj.Buid = "Created Material";
            obj.MaterialGroup = materialGroup;
            obj.Name = "Created Material";

            return new TestData(xml, xmlCS, obj);
        }

        public TestData getCreatedChildEntityTestData()
        {
            String dataName = "CreatedChildEntity";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = xml.Replace("\"Object\"", "\"IChangeContainer\"");

            MaterialGroup materialGroup = EntityFactory.CreateEntity<MaterialGroup>();
            materialGroup.Buid = "new mg";
            materialGroup.Name = "new mg name";

            Material obj = cache.GetObject<Material>(1);
            obj.MaterialGroup = materialGroup;

            return new TestData(xml, xmlCS, obj);
        }

        public TestData getCreatedChildEntityTestData2()
        {
            String dataName = "CreatedChildEntity2";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = xml.Replace("n=\"Object\"", "n=\"IChangeContainer\"");
            TestData testData = getCreatedChildEntityTestData();
            return new TestData(xml, xmlCS, testData.obj);
        }

        public TestData getCreatedParentAndChildEntitiesTestData()
        {
            String dataName = "CreatedParentAndChildEntities";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);

            MaterialGroup materialGroup = EntityFactory.CreateEntity<MaterialGroup>();
            materialGroup.Buid = "new mg";
            materialGroup.Name = "new mg name";

            Material obj = EntityFactory.CreateEntity<Material>();
            obj.Buid = "Created Material";
            obj.MaterialGroup = materialGroup;
            obj.Name = "Created Material";

            return new TestData(xml, xmlCS, obj);
        }

        public TestData getCreatedParentAndChildEntitiesTestData2()
        {
            String dataName = "CreatedParentAndChildEntities2";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);
            TestData testData = getCreatedParentAndChildEntitiesTestData();
            return new TestData(xml, xmlCS, testData.obj);
        }

        public TestData getCreatedParentAndChildEntitiesInListTestData()
        {
            String dataName = "CreatedParentAndChildEntitiesInList";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);

            MaterialGroup materialGroup = EntityFactory.CreateEntity<MaterialGroup>();
            materialGroup.Buid = "new mg";
            materialGroup.Name = "new mg name";

            Material newMaterial = EntityFactory.CreateEntity<Material>();
            newMaterial.Buid = "Created Material";
            newMaterial.MaterialGroup = materialGroup;
            newMaterial.Name = "Created Material";

            IList<Object> obj = new List<Object>();
            obj.Add(newMaterial);
            obj.Add(materialGroup);

            return new TestData(xml, xmlCS, obj);
        }

        public TestData getCreatedParentAndChildEntitiesInListTestData2()
        {
            String dataName = "CreatedParentAndChildEntitiesInList2";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);
            TestData testData = getCreatedParentAndChildEntitiesInListTestData();
            return new TestData(xml, xmlCS, testData.obj);
        }

        public TestData getMultipleCreatedEntitiesTestData()
        {
            String dataName = "MultipleCreatedEntities";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);

            MaterialGroup materialGroup = cache.GetObject<MaterialGroup>("1");

            Material newMaterial1 = EntityFactory.CreateEntity<Material>();
            newMaterial1.Buid = "Created Material";
            newMaterial1.MaterialGroup = materialGroup;
            newMaterial1.Name = "Created Material";

            Material newMaterial2 = EntityFactory.CreateEntity<Material>();
            newMaterial2.Buid = "Created Material 2";
            newMaterial2.MaterialGroup = materialGroup;
            newMaterial2.Name = "Created Material 2";

            IList<Object> obj = new List<Object>();
            obj.Add(newMaterial1);
            obj.Add(newMaterial2);

            return new TestData(xml, xmlCS, obj);
        }

        public TestData getMultipleCreatedEntitiesTestData2()
        {
            String dataName = "MultipleCreatedEntities2";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);
            TestData testData = getMultipleCreatedEntitiesTestData();
            return new TestData(xml, xmlCS, testData.obj);
        }

        public TestData getCreatedAndExistingChildrenTestData()
        {
            String dataName = "CreatedAndExistingChildren";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = xml.Replace("\"Object\"", "\"IChangeContainer\"");

            EntityA entityA = cache.GetObject<EntityA>(1);
            EntityB entityB = EntityFactory.CreateEntity<EntityB>();
            IList<EntityB> entityBs = entityA.EntityBs;
            entityBs.Add(entityB);

            return new TestData(xml, xmlCS, entityA);
        }

        public TestData getCreatedAndExistingChildrenTestData2()
        {
            String dataName = "CreatedAndExistingChildren2";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = xml.Replace("\"Object\"", "\"IChangeContainer\"");
            TestData testData = getCreatedAndExistingChildrenTestData();
            return new TestData(xml, xmlCS, testData.obj);
        }

        public TestData getUpdatedEntityTestData()
        {
            String dataName = "UpdatedEntity";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = xml.Replace("\"Object\"", "\"IChangeContainer\"");
            Material obj = cache.GetObject<Material>(1);
            obj.Name = obj.Name + ".2";
            return new TestData(xml, xmlCS, obj);
        }

        public TestData getCreatedAndUpdatedEntitiesTestData()
        {
            String dataName = "CreatedAndUpdatedEntities";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);

            Material material = cache.GetObject<Material>(1);
            material.Name = "Material 1.2";

            MaterialGroup materialGroup = cache.GetObject<MaterialGroup>("1");

            Material newMaterial = EntityFactory.CreateEntity<Material>();
            newMaterial.Buid = "Created Material";
            newMaterial.MaterialGroup = materialGroup;
            newMaterial.Name = "Created Material";

            IList<Object> obj = new List<Object>();
            obj.Add(material);
            obj.Add(newMaterial);

            return new TestData(xml, xmlCS, obj);
        }

        public TestData getCreatedAndUpdatedEntitiesTestData2()
        {
            String dataName = "CreatedAndUpdatedEntities2";
            String xml = GetJavaTestXml(dataName);
            String xmlCS = GetCsTestXml(dataName);
            TestData testData = getCreatedAndUpdatedEntitiesTestData();
            return new TestData(xml, xmlCS, testData.obj);
        }

        protected Object[] ColletionData
        {
            get
            {
                Material material = cache.GetObject<Material>(1);
                MaterialGroup materialGroup = cache.GetObject<MaterialGroup>("1");
                return new Object[] { 3, material, "test", materialGroup, material, new DateTime(2001, 1, 1, 0, 0, 0, DateTimeKind.Utc) };
            }
        }

        protected String GetJavaTestXml(String dataName)
        {
            return Properties.GetString(String.Format(TEST_DATA_JAVA, dataName)).Trim();
        }

        protected String GetCsTestXml(String dataName)
        {
            return Properties.GetString(String.Format(TEST_DATA_CS, dataName)).Trim();
        }
    }
}
