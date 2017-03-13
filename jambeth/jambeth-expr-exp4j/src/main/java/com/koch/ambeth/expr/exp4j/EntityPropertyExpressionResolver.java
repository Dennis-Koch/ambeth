package com.koch.ambeth.expr.exp4j;

import com.koch.ambeth.expr.IEntityPropertyExpressionResolver;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.config.IProperties;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

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
