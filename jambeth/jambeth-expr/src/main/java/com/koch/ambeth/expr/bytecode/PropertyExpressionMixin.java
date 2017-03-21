package com.koch.ambeth.expr.bytecode;

/*-
 * #%L
 * jambeth-expr
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
import com.koch.ambeth.expr.PropertyExpression;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.util.IConversionHelper;

public class PropertyExpressionMixin {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired(optional = true)
	protected IEntityPropertyExpressionResolver entityPropertyExpressionResolver;

	protected boolean firstUsage = true;

	public Object evaluate(IEntityMetaDataHolder entity, String expression, Class<?> expectedType) {
		if (entityPropertyExpressionResolver == null) {
			if (firstUsage) {
				firstUsage = false;
				log.warn("INACTIVE: Annotation feature @" + PropertyExpression.class.getName()
						+ " on entities. Reason: No instance of "
						+ IEntityPropertyExpressionResolver.class.getName() + " resolved");
			}
			return conversionHelper.convertValueToType(expectedType, Integer.valueOf(0));
		}
		Object expressionResult =
				entityPropertyExpressionResolver.resolveExpressionOnEntity(entity, expression);
		return conversionHelper.convertValueToType(expectedType, expressionResult);
	}
}
