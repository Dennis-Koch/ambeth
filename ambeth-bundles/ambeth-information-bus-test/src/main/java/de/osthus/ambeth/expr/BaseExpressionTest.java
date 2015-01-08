package de.osthus.ambeth.expr;

import org.junit.Assert;

import de.osthus.ambeth.cache.imc.InMemoryCacheRetriever;
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

@TestModule(ExpressionTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/expr/orm.xml")
public abstract class BaseExpressionTest extends AbstractInformationBusTest
{
	public static class ExpressionTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			IBeanConfiguration inMemoryCacheRetriever = beanContextFactory.registerBean(InMemoryCacheRetriever.class);
			beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class).with(ExpressionEntity.class);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

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
}
