package de.osthus.ambeth.expr;

import org.junit.Assert;

import de.osthus.ambeth.cache.imc.InMemoryCacheRetriever;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.expr.BaseExpressionTest.ExpressionTestModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@TestModule(ExpressionTestModule.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/expr/orm.xml"),
		@TestProperties(name = BaseExpressionTest.myFunnyProp, value = "123.456") })
public abstract class BaseExpressionTest extends AbstractInformationBusTest
{
	public static class ExpressionTestModule implements IInitializingModule
	{
		@Autowired
		protected IProperties properties;

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration inMemoryCacheRetriever = beanContextFactory.registerBean(InMemoryCacheRetriever.class);
			beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class).with(ExpressionEntity.class);

			// FIXME
			// beanContextFactory.link((Object) myFunnyProp).to(IPropertyWhitelister.class);
		}
	}

	public static final String myFunnyProp = "myFunnyProp";

	public static final String myFunnyProp2 = "myFunnyProp2";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

	@Property(name = myFunnyProp)
	protected double myFunnyValue;

	public void resolvePredefinedExpressionManually()
	{
		ExpressionEntity entity = entityFactory.createEntity(ExpressionEntity.class);
		long value = 5;
		double factor = 2.3;
		entity.setMyProp1(value);

		Object result = beanContext.getService(IEntityPropertyExpressionResolver.class).resolveExpressionOnEntity(entity, "${MyProp1} * " + factor);
		Assert.assertEquals("" + (value * factor), result.toString());
	}

	public void resolvePredefinedExpressionOnEntity()
	{
		ExpressionEntity entity = entityFactory.createEntity(ExpressionEntity.class);
		long value = 5;
		double factor = 2.3;
		entity.setMyProp1(value);

		double result = entity.getValueForPredefinedExpression();
		Assert.assertEquals(value * factor, result, 0.00000001);
	}

	public void resolvePredefinedExpressionNoOp()
	{
		ExpressionEntity entity = entityFactory.createEntity(ExpressionEntity.class);
		long value = 5;
		entity.setMyProp1(value);

		double result = entity.getValueForPredefinedExpression();
		Assert.assertEquals(0.0, result, 0.00000001);
	}

	public void resolveDynamicExpression()
	{
		ExpressionEntity entity = entityFactory.createEntity(ExpressionEntity.class);
		long value = 7;
		double factor = 4.1;
		entity.setMyProp1(value);
		entity.setMyExpressionProp("${MyProp1} * " + factor);

		double result = entity.calcValueForDynamicExpression();
		Assert.assertEquals(value * factor, result, 0.00000001);
	}

	public void resolveDynamicExpressionWithEnvironment()
	{
		ExpressionEntity entity = entityFactory.createEntity(ExpressionEntity.class);
		long value = 7;
		double factor = 4.1;
		entity.setMyProp1(value);
		entity.setMyExpressionProp("${MyProp1} * " + factor + " * ${" + myFunnyProp + "}");

		double result = entity.calcValueForDynamicExpression();
		Assert.assertEquals(value * factor * myFunnyValue, result, 0.00000001);
	}
}
