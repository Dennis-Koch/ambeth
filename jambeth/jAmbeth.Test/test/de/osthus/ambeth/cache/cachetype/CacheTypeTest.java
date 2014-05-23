package de.osthus.ambeth.cache.cachetype;

import java.util.concurrent.Exchanger;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.cachetype.CacheTypeTest.CacheTypeTestModule;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.IEntityMetaDataFiller;
import de.osthus.ambeth.persistence.jdbc.alternateid.AlternateIdEntity;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("cachetype_data.sql")
@SQLStructure("cachetype_structure.sql")
@TestModule(CacheTypeTestModule.class)
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/cache/cachetype/cachetype_orm.xml")
public class CacheTypeTest extends AbstractPersistenceTest
{
	public static IEntityMetaDataFiller entityMetaDataFiller;

	public static class CacheTypeTestModule implements IInitializingModule
	{
		protected IProxyFactory proxyFactory;

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			ParamChecker.assertNotNull(proxyFactory, "proxyFactory");

			// MethodInterceptor methodInterceptor = new MethodInterceptor()
			// {
			// @Override
			// public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
			// {
			// throw new UnsupportedOperationException("Should never be called");
			// }
			// };

			// IAlternateIdEntityServiceCTPrototype prototypeProxy = proxyFactory.createProxy(IAlternateIdEntityServiceCTPrototype.class, methodInterceptor);
			// IAlternateIdEntityServiceCTSingleton singletonProxy = proxyFactory.createProxy(IAlternateIdEntityServiceCTSingleton.class, methodInterceptor);
			// IAlternateIdEntityServiceCTThreadLocal threadLocalProxy = proxyFactory.createProxy(IAlternateIdEntityServiceCTThreadLocal.class,
			// methodInterceptor);

			beanContextFactory.registerBean("prototypeProxy", IAlternateIdEntityServiceCTPrototype.class).autowireable(
					IAlternateIdEntityServiceCTPrototype.class);
			beanContextFactory.registerBean("singletonProxy", IAlternateIdEntityServiceCTSingleton.class).autowireable(
					IAlternateIdEntityServiceCTSingleton.class);
			beanContextFactory.registerBean("threadLocalProxy", IAlternateIdEntityServiceCTThreadLocal.class).autowireable(
					IAlternateIdEntityServiceCTThreadLocal.class);
		}

		public void setProxyFactory(IProxyFactory proxyFactory)
		{
			this.proxyFactory = proxyFactory;
		}
	}

	protected IRootCache rootCache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(rootCache, "rootCache");
	}

	public void setRootCache(IRootCache rootCache)
	{
		this.rootCache = rootCache;
	}

	// @Test
	// public void prototypeBehavior()
	// {
	// IAlternateIdEntityServiceCTPrototype service =
	// beanContext.getService(IAlternateIdEntityServiceCTPrototype.class);
	//
	// AlternateIdEntity entity = entityFactory.createEntity(AlternateIdEntity.class);
	// entity.setName("MyEntityProto");
	// service.updateAlternateIdEntity(entity);
	//
	// AlternateIdEntity loadedEntity = service.getAlternateIdEntityByName(entity.getName());
	//
	// Assert.assertNotNull("Loaded entity must be valid", loadedEntity);
	// Assert.assertEquals("Id must be equal", entity.getId(), loadedEntity.getId());
	//
	// AlternateIdEntity loadedEntity2 = service.getAlternateIdEntityByName(entity.getName());
	// Assert.assertNotSame("Loaded entity instances must not be identical", loadedEntity, loadedEntity2);
	// }

	@Test
	public void singletonBehavior()
	{
		IAlternateIdEntityServiceCTSingleton service = beanContext.getService(IAlternateIdEntityServiceCTSingleton.class);

		AlternateIdEntity entity = entityFactory.createEntity(AlternateIdEntity.class);
		entity.setName("MyEntityNameSingle");
		service.updateAlternateIdEntity(entity);

		AlternateIdEntity loadedEntity = service.getAlternateIdEntityByName(entity.getName());

		Assert.assertNotNull("Loaded entity must be valid", loadedEntity);
		Assert.assertEquals("Id must be equal", entity.getId(), loadedEntity.getId());

		AlternateIdEntity loadedEntity2 = service.getAlternateIdEntityByName(entity.getName());
		Assert.assertSame("Loaded entity instances must be identical", loadedEntity, loadedEntity2);
	}

	@Test
	public void threadLocalBehavior() throws Exception
	{
		final IAlternateIdEntityServiceCTThreadLocal service = beanContext.getService(IAlternateIdEntityServiceCTThreadLocal.class);

		final AlternateIdEntity entity = entityFactory.createEntity(AlternateIdEntity.class);
		entity.setName("MyEntityThreadLocal");
		service.updateAlternateIdEntity(entity);

		final Exchanger<AlternateIdEntity> entity1Ex = new Exchanger<AlternateIdEntity>();
		final Exchanger<AlternateIdEntity> entity2Ex = new Exchanger<AlternateIdEntity>();

		Thread thread1 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				AlternateIdEntity loadedEntity = service.getAlternateIdEntityByName(entity.getName());

				Assert.assertNotNull("Loaded entity must be valid", loadedEntity);
				Assert.assertEquals("Id must be equal", entity.getId(), loadedEntity.getId());

				AlternateIdEntity loadedEntity2 = service.getAlternateIdEntityByName(entity.getName());
				Assert.assertSame("Loaded entity instances must be identical", loadedEntity, loadedEntity2);

				try
				{
					entity1Ex.exchange(loadedEntity2);
				}
				catch (InterruptedException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		});
		Thread thread2 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				AlternateIdEntity loadedEntity = service.getAlternateIdEntityByName(entity.getName());

				Assert.assertNotNull("Loaded entity must be valid", loadedEntity);
				Assert.assertEquals("Id must be equal", entity.getId(), loadedEntity.getId());

				AlternateIdEntity loadedEntity2 = service.getAlternateIdEntityByName(entity.getName());
				Assert.assertSame("Loaded entity instances must be identical", loadedEntity, loadedEntity2);

				try
				{
					entity2Ex.exchange(loadedEntity2);
				}
				catch (InterruptedException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		});
		thread1.start();
		thread2.start();

		AlternateIdEntity entity1 = entity1Ex.exchange(null);
		AlternateIdEntity entity2 = entity2Ex.exchange(null);

		Assert.assertNotSame("Entities from different threads must not be identical", entity1, entity2);

	}
}
