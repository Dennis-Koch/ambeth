package de.osthus.esmeralda.handler.js.expr;

import com.sun.tools.javac.tree.JCTree.JCInstanceOf;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;

public class JsInstanceOfExpressionHandler extends AbstractExpressionHandler<JCInstanceOf>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCInstanceOf instanceofExpr)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		writer.append("Ambeth.instanceof(");
		languageHelper.writeExpressionTree(instanceofExpr.expr);
		writer.append(", \"");
		String javaTypeName = instanceofExpr.clazz.toString();
		languageHelper.writeType(javaTypeName);
		writer.append("\")");
		context.setTypeOnStack(Boolean.TYPE.getName());
	}
}
