package com.koch.ambeth.expr.exp4j;

/*-
 * #%L
 * jambeth-expr-exp4j
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

public class EntityPropertyExpressionResolver implements IEntityPropertyExpressionResolver {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public Object resolveExpressionOnEntity(Object entity, String expression) {
		final IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entity.getClass());
		final Object fEntity = entity;
		Properties propertiesForEntity = new Properties() {
			@Override
			public Object get(String key, IProperties initiallyCalledProps) {
				if (initiallyCalledProps == null) {
					initiallyCalledProps = this;
				}
				Member member = metaData.getMemberByName(key);
				if (member != null) {
					return member.getValue(fEntity);
				}
				Object propertyValue = dictionary.get(key);
				if (propertyValue == null && parent != null) {
					return parent.get(key, initiallyCalledProps);
				}
				if (!(propertyValue instanceof String)) {
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
