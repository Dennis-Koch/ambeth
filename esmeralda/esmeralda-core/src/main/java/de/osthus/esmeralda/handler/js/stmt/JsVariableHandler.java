package de.osthus.esmeralda.handler.js.stmt;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.js.JsSpecific;
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
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();
		HashSet<String> methodScopeVars = ((JsSpecific) context.getLanguageSpecific()).getMethodScopeVars();

		String name = variableStatement.getName().toString();
		// #1: Removed for the moment. This can be used when var definitions are moved to the top of a method
		// boolean newDefinition = methodScopeVars.add(name);
		methodScopeVars.add(name);

		JCExpression initializer = variableStatement.getInitializer();

		// see #1
		// if (!newDefinition && initializer == null)
		// {
		// return;
		// }

		if (standalone)
		{
			languageHelper.newLineIndent();
		}

		// see #1
		// languageHelper.writeStringIfFalse("var ", !newDefinition);
		writer.append("var ");
		languageHelper.writeVariableName(name);

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
