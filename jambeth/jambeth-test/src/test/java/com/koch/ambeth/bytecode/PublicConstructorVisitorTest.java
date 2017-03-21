package com.koch.ambeth.bytecode;

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

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactoryExtendable;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestRebuildContext;

@TestModule({PublicConstructorVisitorTestModule.class})
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/bytecode/orm.xml")
@TestRebuildContext
public class PublicConstructorVisitorTest extends AbstractInformationBusTest {
	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IImplementAbstractObjectFactoryExtendable implementAbstractObjectFactoryExtendable;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();
		implementAbstractObjectFactoryExtendable.register(IEntityB.class);
		implementAbstractObjectFactoryExtendable.registerBaseType(AbstractEntity.class, IEntityC.class);
	}

	@Test
	public void testDefaultConstructor() {
		EntityA entity = entityFactory.createEntity(EntityA.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> defaultConstructor = declaredConstructors[0];
		Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
	}

	@Test
	public void testDefaultAndNonDefaultConstructor() {
		IEntityB entity = entityFactory.createEntity(IEntityB.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> defaultConstructor = declaredConstructors[0];
		Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
	}

	@Test
	public void testNonDefaultConstructor() {
		EntityC entity = entityFactory.createEntity(EntityC.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> entityFactoryConstructor = declaredConstructors[0];
		Assert.assertEquals(1, entityFactoryConstructor.getParameterTypes().length);
	}

	@Test
	public void testDefaultConstructorOnInterface() {
		IEntityA entity = entityFactory.createEntity(IEntityA.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> defaultConstructor = declaredConstructors[0];
		Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
	}

	@Test
	public void testNonDefaultConstructorOnImplementedInterface() {
		IEntityB entity = entityFactory.createEntity(IEntityB.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> defaultConstructor = declaredConstructors[0];
		Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
	}

	@Test
	public void testNonDefaultConstructorOnImplementedObject() {
		try {
			IEntityC entity = entityFactory.createEntity(IEntityC.class);
			Assert.assertNotNull(entity);

			Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
			Assert.assertEquals(1, declaredConstructors.length);

			Constructor<?> defaultConstructor = declaredConstructors[0];
			Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
		}
		catch (Throwable t) {
			// TODO support orm.xml for interfaces
			// -> java.lang.IllegalArgumentException: No metadata found for entity of type class
			// com.koch.ambeth.bytecode.IEntityC$A1 (class
			// com.koch.ambeth.bytecode.IEntityC$A1)
			Assert.fail(t.getMessage());
		}
	}
}
