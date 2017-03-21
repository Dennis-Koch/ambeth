package com.koch.ambeth.expr;

/*-
 * #%L
 * jambeth-information-bus-test
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

public interface ExpressionEntity {
	int getId();

	int getVersion();

	long getMyProp1();

	void setMyProp1(long myProp1);

	@PropertyExpression("${MyProp1} * 2.3")
	double getValueForPredefinedExpression();

	String getMyExpressionProp();

	void setMyExpressionProp(String myExpressionProp);

	@PropertyExpression("${MyExpressionProp}")
	double calcValueForDynamicExpression();
}
