package de.osthus.ambeth.persistence.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CacheBytecodeModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.MappingModule;
import de.osthus.ambeth.ioc.MergeBytecodeModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.persistence.xml.model.Address;
import de.osthus.ambeth.persistence.xml.model.AddressType;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.persistence.xml.model.EmployeeSmallType;
import de.osthus.ambeth.persistence.xml.model.EmployeeType;
import de.osthus.ambeth.persistence.xml.model.Project;
import de.osthus.ambeth.persistence.xml.model.ProjectType;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.category.ReminderTests;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = IndependentMetaDataComparisonTest.basePath + "independent-orm.xml;"
				+ IndependentMetaDataComparisonTest.basePath + "independent-orm2.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile, value = IndependentMetaDataComparisonTest.basePath
				+ "independent-value-object.xml;" + IndependentMetaDataComparisonTest.basePath + "independent-value-object2.xml"),
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true") })
public class IndependentMetaDataComparisonTest extends AbstractPersistenceTest
{
	public static final String basePath = "de/osthus/ambeth/persistence/xml/";

	private IServiceContext createClientBeanContext()
	{
		IProperties serverProperties = beanContext.getService(IProperties.class);
		Properties baseProps = new Properties(serverProperties);
		baseProps.put(ServiceConfigurationConstants.GenericTransferMapping, "true");
		baseProps.put(ServiceConfigurationConstants.NetworkClientMode, "true");
		baseProps.put(ServiceConfigurationConstants.IndependentMetaData, "true");

		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(baseProps);
		IServiceContext beanContext = bootstrapContext.createService(BytecodeModule.class, CacheModule.class, CacheBytecodeModule.class,
				CacheDataChangeModule.class, EventModule.class, IocModule.class, MappingModule.class, MergeBytecodeModule.class, MergeModule.class,
				ServiceModule.class);

		return beanContext;
	}

	@Autowired
	protected IEntityMetaDataProvider serverFixture;

	private IServiceContext clientBeanContext;

	private IEntityMetaDataProvider clientFixture;

	@Before
	public void setUp() throws Exception
	{
		clientBeanContext = createClientBeanContext();
		clientFixture = clientBeanContext.getService(IEntityMetaDataProvider.class);
	}

	@After
	public void tearDown() throws Exception
	{
		clientFixture = null;
		if (clientBeanContext != null)
		{
			clientBeanContext.dispose();
		}
	}

	@Test
	public void testSetup()
	{
		assertNotSame(serverFixture, clientFixture);
	}

	@Test
	public void testGetMetaDataClassOfQ()
	{
		assertNotNull(serverFixture.getMetaData(Employee.class));
		assertNotNull(serverFixture.getMetaData(Project.class));

		assertNotNull(clientFixture.getMetaData(Employee.class));
		assertNotNull(clientFixture.getMetaData(Project.class));
	}

	@Category(ReminderTests.class)
	@Test
	public void testMetaDataContent()
	{
		Class<?>[] allTypes = { Employee.class, Address.class, Project.class };

		for (Class<?> entityType : allTypes)
		{
			IEntityMetaData serverActual = serverFixture.getMetaData(entityType);
			IEntityMetaData clientActual = clientFixture.getMetaData(entityType);
			assertEquals(serverActual, clientActual);
		}
	}

	@Test
	public void testValueObjectConfig()
	{
		Class<?>[] allValueTypes = { EmployeeType.class, EmployeeSmallType.class, AddressType.class, ProjectType.class };

		for (Class<?> valueType : allValueTypes)
		{
			IValueObjectConfig serverActual = serverFixture.getValueObjectConfig(valueType);
			IValueObjectConfig clientActual = clientFixture.getValueObjectConfig(valueType);
			assertNotNull(valueType.getName() + ":", serverActual);
			assertNotNull(valueType.getName() + ":", clientActual);
			Assert.assertEquals(valueType.getName() + ":", serverActual.getEntityType(), clientActual.getEntityType());
			Assert.assertEquals(valueType.getName() + ":", serverActual.getValueType(), clientActual.getValueType());
		}
	}

	private static void assertEquals(IEntityMetaData expected, IEntityMetaData actual)
	{
		String entityName = expected.getEntityType().getSimpleName();
		Assert.assertEquals(entityName, expected.getEntityType(), actual.getEntityType());
		assertEquals(entityName, expected.getIdMember(), actual.getIdMember());
		assertEquals(entityName, expected.getVersionMember(), actual.getVersionMember());

		assertEquals(entityName, expected.getCreatedByMember(), actual.getCreatedByMember());
		assertEquals(entityName, expected.getCreatedOnMember(), actual.getCreatedOnMember());
		assertEquals(entityName, expected.getUpdatedByMember(), actual.getUpdatedByMember());
		assertEquals(entityName, expected.getUpdatedOnMember(), actual.getUpdatedOnMember());

		Assert.assertEquals(entityName, expected.getAlternateIdCount(), actual.getAlternateIdCount());
		int[][] expectedInicesInPrimitives = expected.getAlternateIdMemberIndicesInPrimitives();
		int[][] actualInicesInPrimitives = actual.getAlternateIdMemberIndicesInPrimitives();
		Assert.assertEquals(entityName, expectedInicesInPrimitives.length, actualInicesInPrimitives.length);
		for (int i = 0; i < expectedInicesInPrimitives.length; i++)
		{
			int[] expectedIIP = expectedInicesInPrimitives[i];
			int[] actualIIP = expectedInicesInPrimitives[i];
			Assert.assertEquals(entityName, expectedIIP.length, actualIIP.length);
			for (int j = expectedIIP.length; j-- > 0;)
			{
				PrimitiveMember expectedTypeInfoItem = expected.getPrimitiveMembers()[expectedIIP[i]];
				PrimitiveMember actualTypeInfoItem = actual.getPrimitiveMembers()[actualIIP[i]];
				Assert.assertEquals(entityName, expectedTypeInfoItem.getName(), actualTypeInfoItem.getName());
			}
		}
		assertEquals(entityName, expected.getAlternateIdMembers(), actual.getAlternateIdMembers());
		assertEquals(entityName, expected.getIdMember(), expected.getIdMemberByIdIndex(ObjRef.PRIMARY_KEY_INDEX));
		assertEquals(entityName, actual.getIdMember(), actual.getIdMemberByIdIndex(ObjRef.PRIMARY_KEY_INDEX));

		assertEquals(entityName, expected.getPrimitiveMembers(), actual.getPrimitiveMembers());
		assertEquals(entityName, expected.getRelationMembers(), actual.getRelationMembers());
		Class<?>[] expectedTypesRelatingToThis = expected.getTypesRelatingToThis();
		Class<?>[] actualTypesRelatingToThis = actual.getTypesRelatingToThis();
		Assert.assertEquals(entityName, expectedTypesRelatingToThis.length, actualTypesRelatingToThis.length);
		List<Class<?>> expectedList = Arrays.asList(expectedTypesRelatingToThis);
		for (Class<?> relating : actualTypesRelatingToThis)
		{
			assertTrue(entityName, expectedList.contains(relating));
		}
	}

	// Asserts the existence of content, not the order.
	private static void assertEquals(String message, Member[] expected, Member[] actual)
	{
		Assert.assertEquals(message, expected.length, actual.length);

		outerLoop: for (Member actualItem : actual)
		{
			for (Member expectedItem : expected)
			{
				if (expectedItem.getName().equals(actualItem.getName()))
				{
					assertEquals(message, expectedItem, actualItem);
					continue outerLoop;
				}
			}
			Assert.fail(message + ": looking for property '" + actualItem.getName() + "'");
		}
	}

	private static void assertEquals(String message, Member expected, Member actual)
	{
		message += "." + expected.getName();
		Assert.assertEquals(message, expected.getName(), actual.getName());
		Assert.assertEquals(message, expected.getNullEquivalentValue(), actual.getNullEquivalentValue());
		Assert.assertEquals(message, expected.canRead(), actual.canRead());
		Assert.assertEquals(message, expected.canWrite(), actual.canWrite());
		Assert.assertEquals(message, expected.getDeclaringType().getName(), actual.getDeclaringType().getName());
		Assert.assertEquals(message, expected.getElementType(), actual.getElementType());
		Assert.assertEquals(message, expected.getRealType(), actual.getRealType());
	}

}
