package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class JsVariableHandler extends AbstractJsStatementHandler<JCVariableDecl> implements IStatementHandlerExtension<JCVariableDecl>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCVariableDecl variableStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		if (standalone)
		{
			languageHelper.newLineIndent();
		}

		writer.append("var ");
		languageHelper.writeVariableName(variableStatement.getName().toString());

		JCExpression initializer = variableStatement.getInitializer();
		if (initializer != null)
		{
			writer.append(" = ");
			languageHelper.writeExpressionTree(initializer);
		}

		if (standalone)
		{
			writer.append(';');
		}
	}
}
