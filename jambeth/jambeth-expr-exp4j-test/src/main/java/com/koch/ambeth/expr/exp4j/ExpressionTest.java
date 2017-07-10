package com.koch.ambeth.expr.exp4j;

/*-
 * #%L
 * jambeth-expr-exp4j-test
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

import org.junit.Test;

import com.koch.ambeth.expr.BaseExpressionTest;
import com.koch.ambeth.expr.exp4j.ioc.Exp4jModule;
import com.koch.ambeth.testutil.TestFrameworkModule;

public class ExpressionTest extends BaseExpressionTest {
	@Override
	@TestFrameworkModule(Exp4jModule.class)
	@Test
	public void resolvePredefinedExpressionManually() {
		super.resolvePredefinedExpressionManually();
	}

	@Override
	@TestFrameworkModule(Exp4jModule.class)
	@Test
	public void resolvePredefinedExpressionOnEntity() {
		super.resolvePredefinedExpressionOnEntity();
	}

	@Override
	@TestFrameworkModule({})
	@Test
	public void resolvePredefinedExpressionNoOp() {
		super.resolvePredefinedExpressionNoOp();
	}

	@Override
	@TestFrameworkModule(Exp4jModule.class)
	@Test
	public void resolveDynamicExpression() {
		super.resolveDynamicExpression();
	}
}
