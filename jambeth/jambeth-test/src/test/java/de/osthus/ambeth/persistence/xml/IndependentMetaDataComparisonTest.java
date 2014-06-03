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

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.UtilConfigurationConstants;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CompositeIdModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.DefaultProxyHelper;
import de.osthus.ambeth.merge.EntityMetaDataProvider;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.IValueObjectConfigExtendable;
import de.osthus.ambeth.merge.ValueObjectMap;
import de.osthus.ambeth.merge.config.EntityMetaDataReader;
import de.osthus.ambeth.merge.config.IEntityMetaDataReader;
import de.osthus.ambeth.merge.config.IndependentEntityMetaDataReader;
import de.osthus.ambeth.merge.config.ValueObjectConfigReader;
import de.osthus.ambeth.merge.model.IEntityLifecycleExtendable;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.orm.IOrmXmlReaderExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;
import de.osthus.ambeth.orm.OrmXmlReader20;
import de.osthus.ambeth.orm.OrmXmlReaderLegathy;
import de.osthus.ambeth.persistence.xml.model.Address;
import de.osthus.ambeth.persistence.xml.model.AddressType;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.persistence.xml.model.EmployeeSmallType;
import de.osthus.ambeth.persistence.xml.model.EmployeeType;
import de.osthus.ambeth.persistence.xml.model.Project;
import de.osthus.ambeth.persistence.xml.model.ProjectType;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.typeinfo.RelationProvider;
import de.osthus.ambeth.typeinfo.TypeInfoProvider;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.XmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;
import de.osthus.ambeth.xml.IXmlTypeHelper;
import de.osthus.ambeth.xml.XmlTypeHelper;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestPropertiesList({
		@TestProperties(name = ConfigurationConstants.mappingFile, value = IndependentMetaDataComparisonTest.basePath + "independent-orm.xml;"
				+ IndependentMetaDataComparisonTest.basePath + "independent-orm2.xml"),
		@TestProperties(name = ConfigurationConstants.valueObjectFile, value = IndependentMetaDataComparisonTest.basePath + "independent-value-object.xml;"
				+ IndependentMetaDataComparisonTest.basePath + "independent-value-object2.xml"),
		@TestProperties(name = ConfigurationConstants.GenericTransferMapping, value = "true") })
public class IndependentMetaDataComparisonTest extends AbstractPersistenceTest
{
	public static final String basePath = "de/osthus/ambeth/persistence/xml/";

	@FrameworkModule
	public static final class ClientTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration valueObjectMap = beanContextFactory.registerAnonymousBean(ValueObjectMap.class);

			beanContextFactory
					.registerBean("independentMetaDataProvider", EntityMetaDataProvider.class)
					.propertyRef("ValueObjectMap", valueObjectMap)
					.autowireable(IEntityMetaDataProvider.class, IValueObjectConfigExtendable.class, IEntityLifecycleExtendable.class,
							IEntityMetaDataExtendable.class);
			beanContextFactory.registerBean(MergeModule.INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class);
			beanContextFactory.registerBean("entityMetaDataReader", EntityMetaDataReader.class).autowireable(IEntityMetaDataReader.class);
			beanContextFactory.registerBean("proxyHelper", DefaultProxyHelper.class).autowireable(IProxyHelper.class);
			beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);
			beanContextFactory.registerBean("typeInfoProvider", TypeInfoProvider.class).autowireable(ITypeInfoProvider.class);
			beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);
			beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);

			beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);
			beanContextFactory.registerBean("xmlTypeHelper", XmlTypeHelper.class).autowireable(IXmlTypeHelper.class);

			beanContextFactory.registerBean("ormXmlReader", ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, IOrmXmlReaderRegistry.class)
					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IOrmXmlReaderExtendable.class)
					.propertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").autowireable(IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class);
			beanContextFactory.registerBean("ormXmlReaderLegathy", OrmXmlReaderLegathy.class);
			beanContextFactory.registerBean("ormXmlReader 2.0", OrmXmlReader20.class);
			beanContextFactory.link("ormXmlReader 2.0").to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);
		}
	}

	private static IServiceContext createClientBeanContext()
	{
		Properties baseProps = new Properties(Properties.getApplication());
		baseProps.put(ConfigurationConstants.mappingFile, IndependentMetaDataComparisonTest.basePath + "independent-orm.xml;"
				+ IndependentMetaDataComparisonTest.basePath + "independent-orm2.xml");
		baseProps.put(ConfigurationConstants.valueObjectFile, IndependentMetaDataComparisonTest.basePath + "independent-value-object.xml;"
				+ IndependentMetaDataComparisonTest.basePath + "independent-value-object2.xml");
		baseProps.put(ConfigurationConstants.GenericTransferMapping, "true");
		baseProps.put(ConfigurationConstants.NetworkClientMode, "true");
		baseProps.put(ConfigurationConstants.IndependentMetaData, "true");
		String bootstrapPropertyFile = Properties.getSystem().getString(UtilConfigurationConstants.BootstrapPropertyFile);
		if (bootstrapPropertyFile != null)
		{
			baseProps.load(bootstrapPropertyFile);
		}
		Properties.loadBootstrapPropertyFile();

		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(baseProps);
		IServiceContext beanContext = bootstrapContext.createService(BytecodeModule.class, ClientTestModule.class, CompositeIdModule.class, EventModule.class,
				IocBootstrapModule.class);

		return beanContext;
	}

	private IServiceContext clientBeanContext;

	private IEntityMetaDataProvider serverFixture;

	private IEntityMetaDataProvider clientFixture;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(serverFixture, "serverFixture");
	}

	public void setServerFixture(IEntityMetaDataProvider serverFixture)
	{
		this.serverFixture = serverFixture;
	}

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
		clientBeanContext.dispose();
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
				ITypeInfoItem expectedTypeInfoItem = expected.getPrimitiveMembers()[expectedIIP[i]];
				ITypeInfoItem actualTypeInfoItem = actual.getPrimitiveMembers()[actualIIP[i]];
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
	private static void assertEquals(String message, ITypeInfoItem[] expected, ITypeInfoItem[] actual)
	{
		Assert.assertEquals(message, expected.length, actual.length);

		outerLoop: for (ITypeInfoItem actualItem : actual)
		{
			for (ITypeInfoItem expectedItem : expected)
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

	private static void assertEquals(String message, ITypeInfoItem expected, ITypeInfoItem actual)
	{
		message += "." + expected.getName();
		Assert.assertEquals(message, expected.getName(), actual.getName());
		Assert.assertEquals(message, expected.getXMLName(), actual.getXMLName());
		Assert.assertEquals(message, expected.getDefaultValue(), actual.getDefaultValue());
		Assert.assertEquals(message, expected.getNullEquivalentValue(), actual.getNullEquivalentValue());
		Assert.assertEquals(message, expected.canRead(), actual.canRead());
		Assert.assertEquals(message, expected.canWrite(), actual.canWrite());
		Assert.assertEquals(message, expected.getDeclaringType(), actual.getDeclaringType());
		Assert.assertEquals(message, expected.getElementType(), actual.getElementType());
		Assert.assertEquals(message, expected.getRealType(), actual.getRealType());
		Assert.assertEquals(message, expected.isXMLIgnore(), actual.isXMLIgnore());
	}

}
