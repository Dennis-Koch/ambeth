package de.osthus.esmeralda.handler.js.expr;

import com.sun.tools.javac.tree.JCTree.JCBinary;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.csharp.expr.BinaryExpressionHandler;

public class JsBinaryExpressionHandler extends BinaryExpressionHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(final JCBinary binary)
	{
		switch (binary.getKind())
		{
			case EQUAL_TO:
			{
				writeSimpleBinary(" === ", binary);
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			case NOT_EQUAL_TO:
			{
				writeSimpleBinary(" !== ", binary);
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			case UNSIGNED_RIGHT_SHIFT:
			{
				writeSimpleBinary(" >>> ", binary);
				break;
			}
			default:
			{
				super.handleExpressionIntern(binary);
			}
		}
	}
}
