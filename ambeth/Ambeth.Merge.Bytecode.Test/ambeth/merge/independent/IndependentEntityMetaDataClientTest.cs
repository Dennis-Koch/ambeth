using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge.Independent;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Typeinfo;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Merge
{
    [TestClass]
    [TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = rootPath + "independent-orm.xml;" + rootPath + "independent-orm2.xml")]
    [TestProperties(Name = ServiceConfigurationConstants.ValueObjectFile, Value = rootPath + "independent-vo-config.xml;" + rootPath + "independent-vo-config2.xml")]
    [TestProperties(Name = ServiceConfigurationConstants.IndependentMetaData, Value = "true")]
    [TestModule(typeof(IndependentEntityMetaDataClientTestModule))]
    public class IndependentEntityMetaDataClientTest : AbstractIndependentClientTest
    {
        protected const String rootPath = @"ambeth\merge\independent\";

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [TestMethod]
        public virtual void TestGetMetaData()
        {
            Assert.IsNotNull(EntityMetaDataProvider.GetMetaData(typeof(EntityA)));
            Assert.IsNotNull(EntityMetaDataProvider.GetMetaData(typeof(EntityB)));
        }

        [TestMethod]
        [ExpectedException(typeof(System.ArgumentException))]
        public virtual void TestGetMetaData_Exception()
        {
            Assert.IsNull(EntityMetaDataProvider.GetMetaData(typeof(String)));
        }

        [TestMethod]
        public virtual void TestGetMetaData_TryOnly()
        {
            Assert.IsNotNull(EntityMetaDataProvider.GetMetaData(typeof(EntityA), true));
            Assert.IsNotNull(EntityMetaDataProvider.GetMetaData(typeof(EntityA), false));
            Assert.IsNull(EntityMetaDataProvider.GetMetaData(typeof(String), true));
        }

        [TestMethod]
        [ExpectedException(typeof(System.ArgumentException))]
        public virtual void TestGetMetaData_TryOnlyWithException()
        {
            Assert.IsNull(EntityMetaDataProvider.GetMetaData(typeof(String), false));
        }

        [TestMethod]
        public virtual void TestGetMetaData_List()
        {
            IList<IEntityMetaData> actual = EntityMetaDataProvider.GetMetaData(new Type[0]);
            Assert.AreEqual(0, actual.Count);
            actual = EntityMetaDataProvider.GetMetaData(new Type[] { typeof(EntityA), typeof(EntityB) });
            Assert.AreEqual(2, actual.Count);
        }

        [TestMethod]
        public virtual void TestMetaDataContent_EntityA()
        {
            IEntityMetaData actual = EntityMetaDataProvider.GetMetaData(typeof(EntityA));

            Assert.AreEqual(typeof(EntityA), actual.EntityType);
            CheckTechnicalProperties(actual);

            Assert.AreEqual(0, actual.GetAlternateIdCount());
            Assert.AreEqual(0, actual.AlternateIdMemberIndicesInPrimitives.Length);
            Assert.AreEqual(0, actual.AlternateIdMembers.Length);
            Assert.AreEqual(actual.IdMember, actual.GetIdMemberByIdIndex(ObjRef.PRIMARY_KEY_INDEX));

            Assert.AreEqual(2, actual.PrimitiveMembers.Length);
            Assert.AreEqual(2, actual.RelationMembers.Length);
            Assert.AreEqual(2, actual.TypesRelatingToThis.Length);
        }

        [Ignore]
        [TestMethod]
        public virtual void TestRegisterValueObjectConfig()
        {
        }

        [Ignore]
        [TestMethod]
        public virtual void TestUnregisterValueObjectConfig()
        {
        }

        [TestMethod]
        public virtual void TestGetValueObjectConfig()
        {
            IValueObjectConfig actual;

            actual = EntityMetaDataProvider.GetValueObjectConfig(typeof(EntityAType));
            Assert.IsNotNull(actual);
            Assert.AreEqual(typeof(EntityA), actual.EntityType);
            Assert.AreEqual(typeof(EntityAType), actual.ValueType);

            actual = EntityMetaDataProvider.GetValueObjectConfig(typeof(EntityBType1));
            Assert.IsNotNull(actual);
            Assert.AreEqual(typeof(EntityB), actual.EntityType);
            Assert.AreEqual(typeof(EntityBType1), actual.ValueType);

            actual = EntityMetaDataProvider.GetValueObjectConfig(typeof(EntityBType2));
            Assert.IsNotNull(actual);
            Assert.AreEqual(typeof(EntityB), actual.EntityType);
            Assert.AreEqual(typeof(EntityBType2), actual.ValueType);

            Assert.IsNull(EntityMetaDataProvider.GetValueObjectConfig(typeof(String)));
        }

        [Ignore]
        [TestMethod]
        public virtual void TestRegisterEntityMetaData()
        {
        }

        [Ignore]
        [TestMethod]
        public virtual void TestUnregisterEntityMetaData()
        {
        }

        [TestMethod]
        public virtual void TestEmbeddedTypeAccess()
        {
            IEntityMetaData actual = EntityMetaDataProvider.GetMetaData(typeof(EntityA));
            ITypeInfoItem nameMember = actual.GetMemberByName("Embedded.Name");
            Assert.IsNotNull(nameMember);

            EntityA entity = new EntityA();
            String newName = "test name";
            nameMember.SetValue(entity, newName);
            Assert.AreEqual(newName, entity.Embedded.Name);
        }

        protected void CheckTechnicalProperties(IEntityMetaData actual)
        {
            Assert.IsNotNull(actual.IdMember);
            Assert.AreEqual("Id", actual.IdMember.Name);
            Assert.IsNotNull(actual.VersionMember);
            Assert.AreEqual("Version", actual.VersionMember.Name);

            Assert.IsNull(actual.CreatedByMember);
            Assert.IsNull(actual.CreatedOnMember);
            Assert.IsNull(actual.UpdatedByMember);
            Assert.IsNull(actual.UpdatedOnMember);
        }
    }
}
