package de.osthus.esmeralda.handler.js.expr;

import com.sun.tools.javac.tree.JCTree.JCTypeCast;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;

public class JsTypeCastExpressionHandler extends AbstractExpressionHandler<JCTypeCast>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCTypeCast expression)
	{
		IConversionContext context = this.context.getCurrent();

		String fqTypeName = astHelper.resolveFqTypeFromTypeName(expression.clazz.toString());

		languageHelper.writeExpressionTree(expression.expr);
		context.setTypeOnStack(fqTypeName);
	}
}
