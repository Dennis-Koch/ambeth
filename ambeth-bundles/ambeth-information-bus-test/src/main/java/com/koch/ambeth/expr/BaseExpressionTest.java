package com.koch.ambeth.expr;

import org.junit.Assert;

import com.koch.ambeth.cache.imc.InMemoryCacheRetriever;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.expr.IEntityPropertyExpressionResolver;
import com.koch.ambeth.expr.BaseExpressionTest.ExpressionTestModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.config.IProperties;

@TestModule(ExpressionTestModule.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/expr/orm.xml"),
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
