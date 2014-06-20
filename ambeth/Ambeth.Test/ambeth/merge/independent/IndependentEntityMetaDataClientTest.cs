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
using De.Osthus.Ambeth.Ioc;

namespace De.Osthus.Ambeth.Merge
{
    [TestClass]
    [TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = rootPath + "independent-orm.xml;" + rootPath + "independent-orm2.xml")]
    [TestProperties(Name = ServiceConfigurationConstants.ValueObjectFile, Value = rootPath + "independent-vo-config.xml;" + rootPath + "independent-vo-config2.xml")]
    [TestProperties(Name = ServiceConfigurationConstants.GenericTransferMapping, Value = "true")]
    public class IndependentEntityMetaDataClientTest : AbstractInformationBusTest
    {
        protected const String rootPath = @"ambeth\merge\independent\";

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [TestMethod]
        public virtual void TestGetMetaData()
        {
            Assert.AssertNotNull(EntityMetaDataProvider.GetMetaData(typeof(EntityA)));
            Assert.AssertNotNull(EntityMetaDataProvider.GetMetaData(typeof(EntityB)));
        }

        [TestMethod]
        [ExpectedException(typeof(System.ArgumentException))]
        public virtual void TestGetMetaData_Exception()
        {
            Assert.AssertNull(EntityMetaDataProvider.GetMetaData(typeof(String)));
        }

        [TestMethod]
        public virtual void TestGetMetaData_TryOnly()
        {
            Assert.AssertNotNull(EntityMetaDataProvider.GetMetaData(typeof(EntityA), true));
            Assert.AssertNotNull(EntityMetaDataProvider.GetMetaData(typeof(EntityA), false));
            Assert.AssertNull(EntityMetaDataProvider.GetMetaData(typeof(String), true));
        }

        [TestMethod]
        [ExpectedException(typeof(System.ArgumentException))]
        public virtual void TestGetMetaData_TryOnlyWithException()
        {
            Assert.AssertNull(EntityMetaDataProvider.GetMetaData(typeof(String), false));
        }

        [TestMethod]
        public virtual void TestGetMetaData_List()
        {
            IList<IEntityMetaData> actual = EntityMetaDataProvider.GetMetaData(new Type[0]);
            Assert.AssertEquals(0, actual.Count);
            actual = EntityMetaDataProvider.GetMetaData(new Type[] { typeof(EntityA), typeof(EntityB) });
            Assert.AssertEquals(2, actual.Count);
        }

        [TestMethod]
        public virtual void TestMetaDataContent_EntityA()
        {
            IEntityMetaData actual = EntityMetaDataProvider.GetMetaData(typeof(EntityA));

            Assert.AssertEquals(typeof(EntityA), actual.EntityType);
            CheckTechnicalProperties(actual);

            Assert.AssertEquals(0, actual.GetAlternateIdCount());
            Assert.AssertEquals(0, actual.AlternateIdMemberIndicesInPrimitives.Length);
            Assert.AssertEquals(0, actual.AlternateIdMembers.Length);
            Assert.AssertEquals(actual.IdMember, actual.GetIdMemberByIdIndex(ObjRef.PRIMARY_KEY_INDEX));

            Assert.AssertEquals(2, actual.PrimitiveMembers.Length);
            Assert.AssertEquals(2, actual.RelationMembers.Length);
            Assert.AssertEquals(2, actual.TypesRelatingToThis.Length);
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
            Assert.AssertNotNull(actual);
            Assert.AssertEquals(typeof(EntityA), actual.EntityType);
            Assert.AssertEquals(typeof(EntityAType), actual.ValueType);

            actual = EntityMetaDataProvider.GetValueObjectConfig(typeof(EntityBType1));
            Assert.AssertNotNull(actual);
            Assert.AssertEquals(typeof(EntityB), actual.EntityType);
            Assert.AssertEquals(typeof(EntityBType1), actual.ValueType);

            actual = EntityMetaDataProvider.GetValueObjectConfig(typeof(EntityBType2));
            Assert.AssertNotNull(actual);
            Assert.AssertEquals(typeof(EntityB), actual.EntityType);
            Assert.AssertEquals(typeof(EntityBType2), actual.ValueType);

            Assert.AssertNull(EntityMetaDataProvider.GetValueObjectConfig(typeof(String)));
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
            Assert.AssertNotNull(nameMember);

            EntityA entity = new EntityA();
            String newName = "test name";
            nameMember.SetValue(entity, newName);
            Assert.AssertEquals(newName, entity.Embedded.Name);
        }

        protected void CheckTechnicalProperties(IEntityMetaData actual)
        {
            Assert.AssertNotNull(actual.IdMember);
            Assert.AssertEquals("Id", actual.IdMember.Name);
            Assert.AssertNotNull(actual.VersionMember);
            Assert.AssertEquals("Version", actual.VersionMember.Name);

            Assert.AssertNull(actual.CreatedByMember);
            Assert.AssertNull(actual.CreatedOnMember);
            Assert.AssertNull(actual.UpdatedByMember);
            Assert.AssertNull(actual.UpdatedOnMember);
        }
    }
}
