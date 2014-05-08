package de.osthus.ambeth.testutil.contextstore;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.ioc.exception.BeanContextInitException;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestRebuildContext;

@TestRebuildContext
@TestModule(ServiceContextStoreTestModule.class)
public class ServiceContextStoreTest extends AbstractIocTest
{
	private static final String CONTEXT_1 = "context1";

	private static final String CONTEXT_2 = "context2";

	private static final String SERVICE_PROPERTY = "Service";

	private IServiceContextStoreConf serviceContextStore;

	@Before
	public void setUp() throws Exception
	{
		serviceContextStore = new ServiceContextStore();
		serviceContextStore.addContext(CONTEXT_1, beanContext);
		serviceContextStore.addContext(CONTEXT_2, beanContext);

		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				// By name to name
				contextStore.injectFrom(CONTEXT_2).bean(ServiceContextStoreTestModule.BLA_DSERVICE).in(CONTEXT_1)
						.intoBean(ServiceContextStoreTestModule.BLA_PROV_1).property(SERVICE_PROPERTY);

				// By name to type
				contextStore.injectFrom(CONTEXT_2).bean(ServiceContextStoreTestModule.BLA_DSERVICE).in(CONTEXT_1).intoBean(BlaDServiceProvider.class)
						.property(SERVICE_PROPERTY);

				// By type to name in same context
				contextStore.injectFrom(CONTEXT_1).bean(BlaDService.class).in(CONTEXT_1).intoBean(ServiceContextStoreTestModule.BLA_PROV_3)
						.property(SERVICE_PROPERTY);

				// By type to type
				contextStore.injectFrom(CONTEXT_2).bean(BlaDService.class).in(CONTEXT_1).intoBean(BlaDServiceProviderImpl.class).property(SERVICE_PROPERTY);

				// Manual
				Object sourceBean = contextStore.getContext(CONTEXT_1).getService(ServiceContextStoreTestModule.BLA_DSERVICE);
				Object targetBean = contextStore.getContext(CONTEXT_2).getService(ServiceContextStoreTestModule.BLA_PROV_5);
				contextStore.inject(sourceBean).into(targetBean).property(SERVICE_PROPERTY);
			}
		}).finish();
	}

	@Test
	public void testProviderEmpty()
	{
		BlaDServiceProvider provider = beanContext.getService(ServiceContextStoreTestModule.BLA_PROV_EMPTY, BlaDServiceProvider.class);
		assertNotNull(provider);
		assertNull(provider.getService());
	}

	@Test
	public void testByNameToName()
	{
		checkProviderContent(ServiceContextStoreTestModule.BLA_PROV_1);
	}

	@Test
	public void testByNameToType()
	{
		checkProviderContent(ServiceContextStoreTestModule.BLA_PROV_2);
	}

	@Test
	public void testByTypeToName()
	{
		checkProviderContent(ServiceContextStoreTestModule.BLA_PROV_3);
	}

	@Test
	public void testByTypeToType()
	{
		checkProviderContent(ServiceContextStoreTestModule.BLA_PROV_4);
	}

	@Test
	public void testManualSetting()
	{
		checkProviderContent(ServiceContextStoreTestModule.BLA_PROV_5);
	}

	private void checkProviderContent(String providerName)
	{
		BlaDService bean = beanContext.getService(BlaDService.class);
		BlaDServiceProvider provider = beanContext.getService(providerName, BlaDServiceProvider.class);
		assertNotNull(provider);
		assertNotNull(provider.getService());
		assertSame(bean, provider.getService());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testContextNameAlreadyInUse()
	{
		serviceContextStore.addContext(CONTEXT_1, beanContext);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncompleteInjectionConfig()
	{
		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				// By name to name, but without property name
				contextStore.injectFrom(CONTEXT_2).bean(ServiceContextStoreTestModule.BLA_DSERVICE).in(CONTEXT_1)
						.intoBean(ServiceContextStoreTestModule.BLA_PROV_1);
			}
		}).finish();
	}

	@Test(expected = IllegalStateException.class)
	public void testNonExistantContext1()
	{
		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				contextStore.injectFrom(CONTEXT_2).bean(ServiceContextStoreTestModule.BLA_DSERVICE).in(CONTEXT_1 + "_")
						.intoBean(ServiceContextStoreTestModule.BLA_PROV_1).property("Service");
			}
		}).finish();
	}

	@Test(expected = IllegalStateException.class)
	public void testNonExistantContext2()
	{
		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				contextStore.injectFrom(CONTEXT_2 + "_").bean(ServiceContextStoreTestModule.BLA_DSERVICE).in(CONTEXT_1)
						.intoBean(ServiceContextStoreTestModule.BLA_PROV_1).property("Service");
			}
		}).finish();
	}

	@Test(expected = BeanContextInitException.class)
	public void testNonExistantBeanName1()
	{
		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				contextStore.injectFrom(CONTEXT_2).bean(ServiceContextStoreTestModule.BLA_DSERVICE + "_").in(CONTEXT_1)
						.intoBean(ServiceContextStoreTestModule.BLA_PROV_1).property("Service");
			}
		}).finish();
	}

	@Test(expected = BeanContextInitException.class)
	public void testNonExistantBeanName2()
	{
		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				contextStore.injectFrom(CONTEXT_2).bean(ServiceContextStoreTestModule.BLA_DSERVICE).in(CONTEXT_1)
						.intoBean(ServiceContextStoreTestModule.BLA_PROV_1 + "_").property("Service");
			}
		}).finish();
	}

	@Test(expected = IllegalStateException.class)
	public void testNonExistantBeanType1()
	{
		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				contextStore.injectFrom(CONTEXT_2).bean(BlaDServicePortType.class).in(CONTEXT_1).intoBean(ServiceContextStoreTestModule.BLA_PROV_1)
						.property("Service");
			}
		}).finish();
	}

	@Test(expected = IllegalStateException.class)
	public void testNonExistantBeanType2()
	{
		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				contextStore.injectFrom(CONTEXT_2).bean(ServiceContextStoreTestModule.BLA_DSERVICE).in(CONTEXT_1).intoBean(BlaDServicePortType.class)
						.property("Service");
			}
		}).finish();
	}

	@Test(expected = IllegalStateException.class)
	public void testNonExistantProperty()
	{
		serviceContextStore.withConfig(new IInterconnectConfig()
		{

			@Override
			public void interconnect(IServiceContextStore contextStore)
			{
				contextStore.injectFrom(CONTEXT_2).bean(ServiceContextStoreTestModule.BLA_DSERVICE).in(CONTEXT_1)
						.intoBean(ServiceContextStoreTestModule.BLA_PROV_1).property("Service2");
			}
		}).finish();
	}
}
