package com.koch.ambeth.cache.cachetype;

/*-
 * #%L
 * jambeth-test
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

import java.util.concurrent.Exchanger;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.cachetype.CacheTypeTest.CacheTypeTestModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.jdbc.alternateid.AlternateIdEntity;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.IProxyFactory;

@SQLData("cachetype_data.sql")
@SQLStructure("cachetype_structure.sql")
@TestModule(CacheTypeTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/cache/cachetype/cachetype_orm.xml")
public class CacheTypeTest extends AbstractInformationBusWithPersistenceTest {
	public static class CacheTypeTestModule implements IInitializingModule {
		@Autowired
		protected IProxyFactory proxyFactory;

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean("prototypeProxy", IAlternateIdEntityServiceCTPrototype.class)
					.autowireable(IAlternateIdEntityServiceCTPrototype.class);
			beanContextFactory.registerBean("singletonProxy", IAlternateIdEntityServiceCTSingleton.class)
					.autowireable(IAlternateIdEntityServiceCTSingleton.class);
			beanContextFactory
					.registerBean("threadLocalProxy", IAlternateIdEntityServiceCTThreadLocal.class)
					.autowireable(IAlternateIdEntityServiceCTThreadLocal.class);
		}
	}

	@Autowired
	protected IRootCache rootCache;

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
	// Assert.assertNotSame("Loaded entity instances must not be identical", loadedEntity,
	// loadedEntity2);
	// }

	@Test
	public void singletonBehavior() {
		IAlternateIdEntityServiceCTSingleton service =
				beanContext.getService(IAlternateIdEntityServiceCTSingleton.class);

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
	public void threadLocalBehavior() throws Exception {
		final IAlternateIdEntityServiceCTThreadLocal service =
				beanContext.getService(IAlternateIdEntityServiceCTThreadLocal.class);

		final AlternateIdEntity entity = entityFactory.createEntity(AlternateIdEntity.class);
		entity.setName("MyEntityThreadLocal");
		service.updateAlternateIdEntity(entity);

		final Exchanger<AlternateIdEntity> entity1Ex = new Exchanger<>();
		final Exchanger<AlternateIdEntity> entity2Ex = new Exchanger<>();

		Thread thread1 = new Thread(new Runnable() {
			@Override
			public void run() {
				AlternateIdEntity loadedEntity = service.getAlternateIdEntityByName(entity.getName());

				Assert.assertNotNull("Loaded entity must be valid", loadedEntity);
				Assert.assertEquals("Id must be equal", entity.getId(), loadedEntity.getId());

				AlternateIdEntity loadedEntity2 = service.getAlternateIdEntityByName(entity.getName());
				Assert.assertSame("Loaded entity instances must be identical", loadedEntity, loadedEntity2);

				try {
					entity1Ex.exchange(loadedEntity2);
				}
				catch (InterruptedException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		});
		Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				AlternateIdEntity loadedEntity = service.getAlternateIdEntityByName(entity.getName());

				Assert.assertNotNull("Loaded entity must be valid", loadedEntity);
				Assert.assertEquals("Id must be equal", entity.getId(), loadedEntity.getId());

				AlternateIdEntity loadedEntity2 = service.getAlternateIdEntityByName(entity.getName());
				Assert.assertSame("Loaded entity instances must be identical", loadedEntity, loadedEntity2);

				try {
					entity2Ex.exchange(loadedEntity2);
				}
				catch (InterruptedException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		});
		thread1.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		thread2.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		thread1.start();
		thread2.start();

		AlternateIdEntity entity1 = entity1Ex.exchange(null);
		AlternateIdEntity entity2 = entity2Ex.exchange(null);

		Assert.assertNotSame("Entities from different threads must not be identical", entity1, entity2);

	}
}
