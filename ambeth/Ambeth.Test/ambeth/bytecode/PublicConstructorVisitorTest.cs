using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.AbstractObject;
using De.Osthus.Ambeth.Cache.Mock;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode
{
    [TestClass]
    [TestModule(typeof(PublicConstructorVisitorTestModule))]
    [TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = "ambeth/bytecode/orm.xml")]
    [TestRebuildContext]
    public class PublicConstructorVisitorTest : AbstractInformationBusTest
    {
        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IImplementAbstractObjectFactoryExtendable ImplementAbstractObjectFactoryExtendable { protected get; set; }

        /**
         * {@inheritDoc}
         */
        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();
            ImplementAbstractObjectFactoryExtendable.Register(typeof(IEntityB));
            ImplementAbstractObjectFactoryExtendable.RegisterBaseType(typeof(AbstractEntity), typeof(IEntityC));
        }

        [TestMethod]
        public void testDefaultConstructor()
        {
            EntityA entity = EntityFactory.CreateEntity<EntityA>();
            Assert.AssertNotNull(entity);

            ConstructorInfo[] declaredConstructors = entity.GetType().GetConstructors(BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);
            Assert.AssertEquals(1, declaredConstructors.Length);

            ConstructorInfo defaultConstructor = declaredConstructors[0];
            Assert.AssertEquals(0, defaultConstructor.GetParameters().Length);
        }

        [TestMethod]
        public void testDefaultAndNonDefaultConstructor()
        {
            IEntityB entity = EntityFactory.CreateEntity<IEntityB>();
            Assert.AssertNotNull(entity);

            ConstructorInfo[] declaredConstructors = entity.GetType().GetConstructors(BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);
            Assert.AssertEquals(1, declaredConstructors.Length);

            ConstructorInfo defaultConstructor = declaredConstructors[0];
            Assert.AssertEquals(0, defaultConstructor.GetParameters().Length);
        }

        [TestMethod]
        public void testNonDefaultConstructor()
        {
            EntityC entity = EntityFactory.CreateEntity<EntityC>();
            Assert.AssertNotNull(entity);

            ConstructorInfo[] declaredConstructors = entity.GetType().GetConstructors(BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);
            Assert.AssertEquals(1, declaredConstructors.Length);

            ConstructorInfo entityFactoryConstructor = declaredConstructors[0];
            Assert.AssertEquals(1, entityFactoryConstructor.GetParameters().Length);
        }

        [TestMethod]
        public void testDefaultConstructorOnInterface()
        {
            IEntityA entity = EntityFactory.CreateEntity<IEntityA>();
            Assert.AssertNotNull(entity);

            ConstructorInfo[] declaredConstructors = entity.GetType().GetConstructors(BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);
            Assert.AssertEquals(1, declaredConstructors.Length);

            ConstructorInfo defaultConstructor = declaredConstructors[0];
            Assert.AssertEquals(0, defaultConstructor.GetParameters().Length);
        }

        [TestMethod]
        public void testNonDefaultConstructorOnImplementedInterface()
        {
            IEntityB entity = EntityFactory.CreateEntity<IEntityB>();
            Assert.AssertNotNull(entity);

            ConstructorInfo[] declaredConstructors = entity.GetType().GetConstructors(BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);
            Assert.AssertEquals(1, declaredConstructors.Length);

            ConstructorInfo defaultConstructor = declaredConstructors[0];
            Assert.AssertEquals(0, defaultConstructor.GetParameters().Length);
        }

        [TestMethod]
        public void testNonDefaultConstructorOnImplementedObject()
        {
            try
            {
                IEntityC entity = EntityFactory.CreateEntity<IEntityC>();
                Assert.AssertNotNull(entity);

                ConstructorInfo[] declaredConstructors = entity.GetType().GetConstructors(BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);
                Assert.AssertEquals(1, declaredConstructors.Length);

                ConstructorInfo defaultConstructor = declaredConstructors[0];
                Assert.AssertEquals(1, defaultConstructor.GetParameters().Length);
            }
            catch (Exception t)
            {
                // TODO support orm.xml for interfaces
                // -> java.lang.IllegalArgumentException: No metadata found for entity of type class de.osthus.ambeth.bytecode.IEntityC$A1 (class
                // de.osthus.ambeth.bytecode.IEntityC$A1)
                Assert.Fail(t.Message);
            }
        }
    }
}