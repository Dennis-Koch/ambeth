using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Bytecode
{
    [TestClass]
	[TestProperties(Name = ServiceConfigurationConstants.MappingFile, Value = "ambeth/bytecode/ConstructorBytecodeTest-orm.xml")]
	[TestRebuildContext]
	public class ConstructorBytecodeTest : AbstractInformationBusTest
	{
		[Autowired]
		public IEntityFactory EntityFactory { protected get; set; }

		[TestMethod]
		public void TestWithNonDefaultConstructor()
		{
			TestEntityWithNonDefaultConstructor testEntity = EntityFactory.CreateEntity<TestEntityWithNonDefaultConstructor>();

			Assert.AssertNotNull(testEntity);
		}

		[TestMethod]
		public void TestIsToBeCreated()
		{
			TestEntityWithNonDefaultConstructor testEntity = EntityFactory.CreateEntity<TestEntityWithNonDefaultConstructor>();

			Assert.IsInstanceOfType(testEntity, typeof(IDataObject));
			Assert.AssertTrue(((IDataObject) testEntity).HasPendingChanges);
		}
	}
}