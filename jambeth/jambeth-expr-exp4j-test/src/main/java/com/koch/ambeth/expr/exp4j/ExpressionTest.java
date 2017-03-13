package com.koch.ambeth.expr.exp4j;

import org.junit.Test;

import com.koch.ambeth.expr.BaseExpressionTest;
import com.koch.ambeth.expr.exp4j.ioc.Exp4jModule;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.TestFrameworkModule;

public class ExpressionTest extends BaseExpressionTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	@TestFrameworkModule(Exp4jModule.class)
	@Test
	public void resolvePredefinedExpressionManually()
	{
		super.resolvePredefinedExpressionManually();
	}

	@Override
	@TestFrameworkModule(Exp4jModule.class)
	@Test
	public void resolvePredefinedExpressionOnEntity()
	{
		super.resolvePredefinedExpressionOnEntity();
	}

	@Override
	@TestFrameworkModule({})
	@Test
	public void resolvePredefinedExpressionNoOp()
	{
		super.resolvePredefinedExpressionNoOp();
	}

	@Override
	@TestFrameworkModule(Exp4jModule.class)
	@Test
	public void resolveDynamicExpression()
	{
		super.resolveDynamicExpression();
	}
}
