package de.osthus.ambeth.ioc.link;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.exception.LinkException;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.TestRebuildContext;

@TestRebuildContext
public class LinkContainerTest extends AbstractIocTest
{
	public static final String REGISTRY_NAME = "testRegistryBeanName";

	public static final String REGISTRY_PROPERTY_NAME = "TestListener";

	public static final String LISTENER_NAME = "testListenerBeanName";

	public static final String ListenerProp = "listenerProp";

	public static final String ListenerNameProp = "listenerNameProp";

	public static final String RegistryProp = "registryProp";

	public static final String OptionalProp = "optionalProp";

	public static final String ExtendableTypeProp = "extendableTypeProp";

	public static enum ListenerVariant
	{
		BY_NAME, BY_NAME_AND_METHOD, BY_CONF, BY_INSTANCE
	}

	public static enum RegistryVariant
	{
		BY_EXTENDABLE, BY_NAME_AND_EXTENDABLE, BY_NAME_AND_PROPERTY, BY_NAME_AND_EVENT, BY_INSTANCE_AND_EXTENDABLE, BY_INSTANCE_AND_PROPERTY, BY_INSTANCE_AND_EVENT
	}

	public static class LinkContainerTestModule implements IInitializingModule
	{
		protected Class<?> extendableType;

		protected ListenerVariant listenerVariant;

		protected RegistryVariant registryVariant;

		protected String listenerName;

		protected boolean optional;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration registryC = beanContextFactory.registerBean(REGISTRY_NAME, TestRegistry.class).autowireable(ITestListenerExtendable.class,
					ITestRegistry.class);
			IBeanConfiguration listenerC = beanContextFactory.registerBean(LISTENER_NAME, TestListener.class);

			if (listenerName == null)
			{
				listenerName = LISTENER_NAME;
			}
			ILinkRegistryNeededConfiguration<?> link1;
			switch (listenerVariant)
			{
				case BY_NAME:
					link1 = beanContextFactory.link(listenerName);
					break;
				case BY_NAME_AND_METHOD:
					link1 = beanContextFactory.link(listenerName, "handlePropertyChangedTest");
					break;
				case BY_CONF:
					link1 = beanContextFactory.link(listenerC);
					break;
				case BY_INSTANCE:
					link1 = beanContextFactory.link(listenerC.getInstance());
					break;
				default:
					throw new IllegalArgumentException("Unsupported enum: " + listenerVariant);
			}

			if (extendableType == null)
			{
				extendableType = ITestListenerExtendable.class;
			}
			ILinkConfiguration link2;
			switch (registryVariant)
			{
				case BY_EXTENDABLE:
					link2 = link1.to(extendableType);
					break;
				case BY_NAME_AND_EXTENDABLE:
					link2 = link1.to(REGISTRY_NAME, extendableType);
					break;
				case BY_NAME_AND_EVENT:
					link2 = link1.to(REGISTRY_NAME, new IEventDelegate()
					{
						@Override
						public String getEventName()
						{
							return REGISTRY_PROPERTY_NAME;
						}
					});
					break;
				case BY_NAME_AND_PROPERTY:
					link2 = link1.to(REGISTRY_NAME, REGISTRY_PROPERTY_NAME);
					break;
				case BY_INSTANCE_AND_EXTENDABLE:
					link2 = link1.to(registryC.getInstance(), extendableType);
					break;
				case BY_INSTANCE_AND_EVENT:
					link2 = link1.to(registryC.getInstance(), new IEventDelegate()
					{
						@Override
						public String getEventName()
						{
							return REGISTRY_PROPERTY_NAME;
						}
					});
					break;
				case BY_INSTANCE_AND_PROPERTY:
					link2 = link1.to(registryC.getInstance(), REGISTRY_PROPERTY_NAME);
					break;
				default:
					throw new IllegalArgumentException("Unsupported enum: " + registryVariant);
			}
			if (optional)
			{
				link2 = link2.optional();
			}
		}

		@Property(name = ListenerProp)
		public void setListenerVariant(ListenerVariant listenerVariant)
		{
			this.listenerVariant = listenerVariant;
		}

		@Property(name = ListenerNameProp, mandatory = false)
		public void setListenerName(String listenerName)
		{
			this.listenerName = listenerName;
		}

		@Property(name = RegistryProp)
		public void setRegistryVariant(RegistryVariant registryVariant)
		{
			this.registryVariant = registryVariant;
		}

		@Property(name = OptionalProp, defaultValue = "false")
		public void setOptional(boolean optional)
		{
			this.optional = optional;
		}

		@Property(name = ExtendableTypeProp, mandatory = false)
		public void setExtendableType(Class<?> extendableType)
		{
			this.extendableType = extendableType;
		}
	}

	public static int listenerReceivedCount;

	public static int propertyChangedReceivedCount;

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext childContext;

	protected ITestRegistry testRegistry;

	@Before
	public void setUp()
	{
		childContext = beanContext.createService(LinkContainerTestModule.class);
		testRegistry = childContext.getService(ITestRegistry.class);
	}

	@After
	public void tearDown()
	{
		listenerReceivedCount = 0;
		propertyChangedReceivedCount = 0;
		if (childContext != null)
		{
			childContext.dispose();
			childContext = null;
		}
	}

	protected void testValidContext(int expectedCount)
	{
		testValidContext(expectedCount, expectedCount, 0);
	}

	protected void testValidContext(int expectedCount, int expectedListenerReceivedCount, int expectedPropertyChangedReceivedCount)
	{
		Assert.assertEquals(expectedCount, testRegistry.getTestListeners().length);
		Assert.assertEquals(expectedListenerReceivedCount, listenerReceivedCount);
		Assert.assertEquals(expectedPropertyChangedReceivedCount, propertyChangedReceivedCount);
	}

	protected void testValidContextEvent(int expectedCount)
	{
		Assert.assertEquals(expectedCount, testRegistry.getPceListeners().length);
		Assert.assertEquals(expectedCount, propertyChangedReceivedCount);
	}

	@Test(expected = LinkException.class)
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_EXTENDABLE"),
			@TestProperties(name = ExtendableTypeProp, value = "de.osthus.ambeth.ioc.link.ITestListenerExtendable2") })
	@Ignore
	public void test_NoRByType()
	{
	}

	public static class LinkContainerTestModuleOptional_NoRByType implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean("testListener", TestListener.class);
			beanContextFactory.link("testListener").to(ITestListenerExtendable2.class).optional();
		}
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_EXTENDABLE"),
			@TestProperties(name = ExtendableTypeProp, value = "de.osthus.ambeth.ioc.link.ITestListenerExtendable2"),
			@TestProperties(name = OptionalProp, value = "true") })
	public void testOptional_NoRByType()
	{
		// If no exception occurs everything is ok
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_EXTENDABLE"),
			@TestProperties(name = ListenerNameProp, value = LISTENER_NAME + "_wrong_name"), @TestProperties(name = OptionalProp, value = "true") })
	public void testOptional_NoL()
	{
		// If no exception occurs everything is ok
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_EXTENDABLE") })
	public void testLByName_RByExtendable()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_EXTENDABLE") })
	public void testLByName_RByNameAndExtendable()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_EVENT") })
	public void testLByName_RByNameAndEvent()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY") })
	public void testLByName_RByNameAndProperty()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EXTENDABLE") })
	public void testLByName_RByInstanceAndExtendable()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EVENT") })
	public void testLByName_RByInstanceAndEvent()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME"), @TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_PROPERTY") })
	public void testLByName_RByInstanceAndProperty()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"), @TestProperties(name = RegistryProp, value = "BY_EXTENDABLE") })
	public void testLByNameAndMethod_RByExtendable()
	{
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EXTENDABLE") })
	public void testLByNameAndMethod_RByNameAndExtendable()
	{
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_EVENT") })
	public void testLByNameAndMethod_RByNameAndEvent()
	{
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY") })
	public void testLByNameAndMethod_RByNameAndProperty()
	{
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EXTENDABLE") })
	public void testLByNameAndMethod_RByInstanceAndExtendable()
	{
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EVENT") })
	public void testLByNameAndMethod_RByInstanceAndEvent()
	{
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_PROPERTY") })
	public void testLByNameAndMethod_RByInstanceAndProperty()
	{
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_CONF"), @TestProperties(name = RegistryProp, value = "BY_EXTENDABLE") })
	public void testLByConf_RByExtendable()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_CONF"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_EXTENDABLE") })
	public void testLByConf_RByNameAndExtendable()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_CONF"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_EVENT") })
	public void testLByConf_RByNameAndEvent()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_CONF"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY") })
	public void testLByConf_RByNameAndProperty()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_CONF"), @TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EXTENDABLE") })
	public void testLByConf_RByInstanceAndExtendable()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_CONF"), @TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EVENT") })
	public void testLByConf_RByInstanceAndEvent()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_CONF"), @TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_PROPERTY") })
	public void testLByConf_RByInstanceAndProperty()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_INSTANCE"), @TestProperties(name = RegistryProp, value = "BY_EXTENDABLE") })
	public void testLByInstance_RByExtendable()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_INSTANCE"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_EXTENDABLE") })
	public void testLByInstance_RByNameAndExtendable()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_INSTANCE"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_EVENT") })
	public void testLByInstance_RByNameAndEvent()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_INSTANCE"), @TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY") })
	public void testLByInstance_RByNameAndProperty()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EXTENDABLE") })
	public void testLByInstance_RByInstance()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_INSTANCE"), @TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EVENT") })
	public void testLByInstance_RByInstanceAndEvent()
	{
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({ @TestProperties(name = ListenerProp, value = "BY_INSTANCE"), @TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_PROPERTY") })
	public void testLByInstance_RByInstanceAndProperty()
	{
		testValidContext(1);
	}
}
