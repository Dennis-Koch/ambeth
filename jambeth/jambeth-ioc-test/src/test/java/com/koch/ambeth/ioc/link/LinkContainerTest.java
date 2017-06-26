package com.koch.ambeth.ioc.link;

/*-
 * #%L
 * jambeth-ioc-test
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.exception.LinkException;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

@TestRebuildContext
public class LinkContainerTest extends AbstractIocTest {
	public static final String REGISTRY_NAME = "testRegistryBeanName";

	public static final String REGISTRY_PROPERTY_NAME = "TestListener";

	public static final String LISTENER_NAME = "testListenerBeanName";

	public static final String ContextProp = "contextProp";

	public static final String ListenerProp = "listenerProp";

	public static final String ListenerNameProp = "listenerNameProp";

	public static final String RegistryProp = "registryProp";

	public static final String OptionalProp = "optionalProp";

	public static final String ExtendableTypeProp = "extendableTypeProp";

	public static final String FOREIGN_CONTEXT_NAME = "funnyContextName";

	public static enum ContextVariant {
		NONE, BY_NAME, BY_REFERENCE
	}

	public static enum ListenerVariant {
		BY_NAME, BY_NAME_AND_METHOD, BY_CONF, BY_INSTANCE
	}

	public static enum RegistryVariant {
		BY_EXTENDABLE, BY_NAME_AND_EXTENDABLE, BY_NAME_AND_PROPERTY, BY_NAME_AND_EVENT, BY_INSTANCE_AND_EXTENDABLE, BY_INSTANCE_AND_PROPERTY, BY_INSTANCE_AND_EVENT, BY_AUTOWIRING_AND_EXTENDABLE, BY_AUTOWIRING_AND_PROPERTY, BY_AUTOWIRING_AND_EVENT;
	}

	public static class ForeignContainerTestModule implements IInitializingModule {
		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(REGISTRY_NAME, TestRegistry.class)
					.autowireable(ITestListenerExtendable.class, ITestRegistry.class);
		}
	}

	public static class LinkContainerTestModule implements IInitializingModule {
		@Property(name = ExtendableTypeProp, mandatory = false)
		protected Class<?> extendableType;

		@Property(name = ListenerProp)
		protected ListenerVariant listenerVariant;

		@Property(name = RegistryProp)
		protected RegistryVariant registryVariant;

		@Property(name = ListenerNameProp, mandatory = false)
		protected String listenerName;

		@Property(name = OptionalProp, defaultValue = "false")
		protected boolean optional;

		@Property(name = ContextProp, defaultValue = "NONE")
		protected ContextVariant toContext;

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			IBeanConfiguration registryC =
					beanContextFactory.registerBean(REGISTRY_NAME, TestRegistry.class)
							.autowireable(ITestListenerExtendable.class, ITestRegistry.class);
			IBeanConfiguration listenerC =
					beanContextFactory.registerBean(LISTENER_NAME, TestListener.class);

			if (listenerName == null) {
				listenerName = LISTENER_NAME;
			}
			ILinkRegistryNeededConfiguration<?> link1;
			switch (listenerVariant) {
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
					throw RuntimeExceptionUtil.createEnumNotSupportedException(listenerVariant);
			}

			if (extendableType == null) {
				extendableType = ITestListenerExtendable.class;
			}
			switch (toContext) {
				case NONE:
					break;
				case BY_NAME:
					link1 = link1.toContext(LinkContainerTest.foreignContext);
					break;
				case BY_REFERENCE:
					link1 = link1.toContext(LinkContainerTest.FOREIGN_CONTEXT_NAME);
					break;
				default:
					throw RuntimeExceptionUtil.createEnumNotSupportedException(toContext);
			}
			ILinkConfigWithOptional link2;
			switch (registryVariant) {
				case BY_EXTENDABLE:
					link2 = link1.to(extendableType);
					break;
				case BY_NAME_AND_EXTENDABLE:
					link2 = link1.to(REGISTRY_NAME, extendableType);
					break;
				case BY_NAME_AND_EVENT:
					link2 = link1.to(REGISTRY_NAME, new IEventDelegate() {
						@Override
						public String getEventName() {
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
					link2 = link1.to(registryC.getInstance(), new IEventDelegate() {
						@Override
						public String getEventName() {
							return REGISTRY_PROPERTY_NAME;
						}
					});
					break;
				case BY_INSTANCE_AND_PROPERTY:
					link2 = link1.to(registryC.getInstance(), REGISTRY_PROPERTY_NAME);
					break;
				case BY_AUTOWIRING_AND_EXTENDABLE:
					link2 = link1.to(ITestRegistry.class, extendableType);
					break;
				case BY_AUTOWIRING_AND_EVENT:
					link2 = link1.to(ITestRegistry.class, new IEventDelegate() {
						@Override
						public String getEventName() {
							return REGISTRY_PROPERTY_NAME;
						}
					});
					break;
				case BY_AUTOWIRING_AND_PROPERTY:
					link2 = link1.to(ITestRegistry.class, REGISTRY_PROPERTY_NAME);
					break;
				default:
					throw RuntimeExceptionUtil.createEnumNotSupportedException(registryVariant);
			}
			if (optional) {
				link2.optional();
				link2 = null;
			}
		}
	}

	public static int listenerReceivedCount;

	public static int propertyChangedReceivedCount;

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext childContext;

	public static IServiceContext foreignContext;

	protected ITestRegistry testRegistry, foreignTestRegistry;

	@Before
	public void setUp() {
		foreignContext = beanContext.createService(ForeignContainerTestModule.class);
		foreignTestRegistry = foreignContext.getService(ITestRegistry.class);

		childContext =
				beanContext.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>() {
					@Override
					public void invoke(IBeanContextFactory beanContextFactory) throws Exception {
						beanContextFactory.registerExternalBean(FOREIGN_CONTEXT_NAME, foreignContext);
					}
				}, LinkContainerTestModule.class);
		testRegistry = childContext.getService(ITestRegistry.class);
	}

	@After
	public void tearDown() {
		listenerReceivedCount = 0;
		propertyChangedReceivedCount = 0;
		if (childContext != null) {
			childContext.dispose();
			childContext = null;
		}
		if (foreignContext != null) {
			foreignContext.dispose();
			foreignContext = null;
		}
	}

	protected void testValidContext(int expectedCount) {
		testValidContext(expectedCount, expectedCount, 0);
	}

	protected void testValidContext(int expectedCount, int expectedListenerReceivedCount,
			int expectedPropertyChangedReceivedCount) {
		Assert.assertEquals(expectedCount, testRegistry.getTestListeners().length);
		Assert.assertEquals(expectedListenerReceivedCount, listenerReceivedCount);
		Assert.assertEquals(expectedPropertyChangedReceivedCount, propertyChangedReceivedCount);
	}

	protected void testValidContextEvent(int expectedCount) {
		Assert.assertEquals(expectedCount, testRegistry.getPceListeners().length);
		Assert.assertEquals(expectedCount, propertyChangedReceivedCount);
	}

	@Test(expected = LinkException.class)
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_EXTENDABLE"), @TestProperties(
					name = ExtendableTypeProp, value = "com.koch.ambeth.ioc.link.ITestListenerExtendable2")})
	@Ignore
	public void test_NoRByType() {
	}

	public static class LinkContainerTestModuleOptional_NoRByType implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean("testListener", TestListener.class);
			beanContextFactory.link("testListener").to(ITestListenerExtendable2.class).optional();
		}
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_EXTENDABLE"),
			@TestProperties(name = ExtendableTypeProp,
					value = "com.koch.ambeth.ioc.link.ITestListenerExtendable2"),
			@TestProperties(name = OptionalProp, value = "true")})
	public void testOptional_NoRByType() {
		// If no exception occurs everything is ok
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_EXTENDABLE"),
			@TestProperties(name = ListenerNameProp, value = LISTENER_NAME + "_wrong_name"),
			@TestProperties(name = OptionalProp, value = "true")})
	public void testOptional_NoL() {
		// If no exception occurs everything is ok
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_EXTENDABLE")})
	public void testLByName_RByExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EXTENDABLE")})
	public void testLByName_RByNameAndExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EVENT")})
	public void testLByName_RByNameAndEvent() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY")})
	public void testLByName_RByNameAndProperty() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EXTENDABLE")})
	public void testLByName_RByInstanceAndExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EVENT")})
	public void testLByName_RByInstanceAndEvent() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_PROPERTY")})
	public void testLByName_RByInstanceAndProperty() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_AUTOWIRING_AND_EXTENDABLE")})
	public void testLByName_RByAutowiringAndExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_AUTOWIRING_AND_EVENT")})
	public void testLByName_RByAutowiringAndEvent() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME"),
			@TestProperties(name = RegistryProp, value = "BY_AUTOWIRING_AND_PROPERTY")})
	public void testLByName_RByAutowiringAndProperty() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_EXTENDABLE")})
	public void testLByNameAndMethod_RByExtendable() {
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EXTENDABLE")})
	public void testLByNameAndMethod_RByNameAndExtendable() {
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EVENT")})
	public void testLByNameAndMethod_RByNameAndEvent() {
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY")})
	public void testLByNameAndMethod_RByNameAndProperty() {
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EXTENDABLE")})
	public void testLByNameAndMethod_RByInstanceAndExtendable() {
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EVENT")})
	public void testLByNameAndMethod_RByInstanceAndEvent() {
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_NAME_AND_METHOD"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_PROPERTY")})
	public void testLByNameAndMethod_RByInstanceAndProperty() {
		testValidContext(1, 0, 1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_CONF"),
			@TestProperties(name = RegistryProp, value = "BY_EXTENDABLE")})
	public void testLByConf_RByExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_CONF"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EXTENDABLE")})
	public void testLByConf_RByNameAndExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_CONF"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EVENT")})
	public void testLByConf_RByNameAndEvent() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_CONF"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY")})
	public void testLByConf_RByNameAndProperty() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_CONF"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EXTENDABLE")})
	public void testLByConf_RByInstanceAndExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_CONF"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EVENT")})
	public void testLByConf_RByInstanceAndEvent() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_CONF"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_PROPERTY")})
	public void testLByConf_RByInstanceAndProperty() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_EXTENDABLE")})
	public void testLByInstance_RByExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EXTENDABLE")})
	public void testLByInstance_RByNameAndExtendable() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_EVENT")})
	public void testLByInstance_RByNameAndEvent() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY")})
	public void testLByInstance_RByNameAndProperty() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EXTENDABLE")})
	public void testLByInstance_RByInstance() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_EVENT")})
	public void testLByInstance_RByInstanceAndEvent() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_INSTANCE_AND_PROPERTY")})
	public void testLByInstance_RByInstanceAndProperty() {
		testValidContext(1);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_AUTOWIRING_AND_EXTENDABLE"),
			@TestProperties(name = ContextProp, value = "BY_NAME")})
	public void testLByInstance_RByAutowiringAndExtendable_OnContextName() {
		testValidContext(0);
		Assert.assertEquals(1, foreignTestRegistry.getTestListeners().length);
	}

	@Test
	@TestPropertiesList({@TestProperties(name = ListenerProp, value = "BY_INSTANCE"),
			@TestProperties(name = RegistryProp, value = "BY_NAME_AND_PROPERTY"),
			@TestProperties(name = ContextProp, value = "BY_REFERENCE")})
	public void testLByInstance_RByNameAndProperty_OnContextReference() {
		testValidContext(0);
		Assert.assertEquals(1, foreignTestRegistry.getTestListeners().length);
	}
}
