package de.osthus.ambeth.expr.exp4j;

import org.junit.Test;

import de.osthus.ambeth.expr.BaseExpressionTest;
import de.osthus.ambeth.ioc.Exp4jModule;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.TestFrameworkModule;

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
