package de.osthus.ambeth.expr.bytecode;

import de.osthus.ambeth.expr.IEntityPropertyExpressionResolver;
import de.osthus.ambeth.expr.PropertyExpression;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.util.IConversionHelper;

public class PropertyExpressionMixin implements IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired(optional = true)
	protected IEntityPropertyExpressionResolver entityPropertyExpressionResolver;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (entityPropertyExpressionResolver == null)
		{
			log.warn("INACTIVE: Annotation feature @" + PropertyExpression.class.getName() + " on entities. Reason: No instance of "
					+ IEntityPropertyExpressionResolver.class.getName() + " resolved");
		}
	}

	public Object evaluate(IEntityMetaDataHolder entity, String expression, Class<?> expectedType)
	{
		if (entityPropertyExpressionResolver == null)
		{
			return conversionHelper.convertValueToType(expectedType, Integer.valueOf(0));
		}
		Object expressionResult = entityPropertyExpressionResolver.resolveExpressionOnEntity(entity, expression);
		return conversionHelper.convertValueToType(expectedType, expressionResult);
	}
}
