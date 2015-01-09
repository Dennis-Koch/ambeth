package de.osthus.ambeth.expr.exp4j;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.expr.IEntityPropertyExpressionResolver;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;

public class EntityPropertyExpressionResolver implements IEntityPropertyExpressionResolver
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public Object resolveExpressionOnEntity(Object entity, String expression)
	{
		final IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entity.getClass());
		final Object fEntity = entity;
		Properties propertiesForEntity = new Properties()
		{
			@Override
			public Object get(String key, IProperties initiallyCalledProps)
			{
				if (initiallyCalledProps == null)
				{
					initiallyCalledProps = this;
				}
				Member member = metaData.getMemberByName(key);
				if (member != null)
				{
					return member.getValue(fEntity);
				}
				Object propertyValue = dictionary.get(key);
				if (propertyValue == null && parent != null)
				{
					return parent.get(key, initiallyCalledProps);
				}
				if (!(propertyValue instanceof String))
				{
					return propertyValue;
				}
				return initiallyCalledProps.resolvePropertyParts((String) propertyValue);
			}
		};
		String resolvedExpression = propertiesForEntity.resolvePropertyParts(expression);

		Expression e = new ExpressionBuilder(resolvedExpression).build();
		double result = e.evaluate();
		return result;
	}
}
