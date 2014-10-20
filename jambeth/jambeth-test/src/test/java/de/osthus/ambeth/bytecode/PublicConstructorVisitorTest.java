package de.osthus.ambeth.bytecode;

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactoryExtendable;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestRebuildContext;

@TestModule({ PublicConstructorVisitorTestModule.class })
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/bytecode/orm.xml")
@TestRebuildContext
public class PublicConstructorVisitorTest extends AbstractInformationBusTest
{
	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IImplementAbstractObjectFactoryExtendable implementAbstractObjectFactoryExtendable;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		implementAbstractObjectFactoryExtendable.register(IEntityB.class);
		implementAbstractObjectFactoryExtendable.registerBaseType(AbstractEntity.class, IEntityC.class);
	}

	@Test
	public void testDefaultConstructor()
	{
		EntityA entity = entityFactory.createEntity(EntityA.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> defaultConstructor = declaredConstructors[0];
		Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
	}

	@Test
	public void testDefaultAndNonDefaultConstructor()
	{
		IEntityB entity = entityFactory.createEntity(IEntityB.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> defaultConstructor = declaredConstructors[0];
		Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
	}

	@Test
	public void testNonDefaultConstructor()
	{
		EntityC entity = entityFactory.createEntity(EntityC.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> entityFactoryConstructor = declaredConstructors[0];
		Assert.assertEquals(1, entityFactoryConstructor.getParameterTypes().length);
	}

	@Test
	public void testDefaultConstructorOnInterface()
	{
		IEntityA entity = entityFactory.createEntity(IEntityA.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> defaultConstructor = declaredConstructors[0];
		Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
	}

	@Test
	public void testNonDefaultConstructorOnImplementedInterface()
	{
		IEntityB entity = entityFactory.createEntity(IEntityB.class);
		Assert.assertNotNull(entity);

		Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
		Assert.assertEquals(1, declaredConstructors.length);

		Constructor<?> defaultConstructor = declaredConstructors[0];
		Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
	}

	@Test
	public void testNonDefaultConstructorOnImplementedObject()
	{
		try
		{
			IEntityC entity = entityFactory.createEntity(IEntityC.class);
			Assert.assertNotNull(entity);

			Constructor<?>[] declaredConstructors = entity.getClass().getDeclaredConstructors();
			Assert.assertEquals(1, declaredConstructors.length);

			Constructor<?> defaultConstructor = declaredConstructors[0];
			Assert.assertEquals(0, defaultConstructor.getParameterTypes().length);
		}
		catch (Throwable t)
		{
			// TODO support orm.xml for interfaces
			// -> java.lang.IllegalArgumentException: No metadata found for entity of type class de.osthus.ambeth.bytecode.IEntityC$A1 (class
			// de.osthus.ambeth.bytecode.IEntityC$A1)
			Assert.fail(t.getMessage());
		}
	}
}
