package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.Lang;

public class CsForEnhancedHandler extends AbstractStatementHandler<JCEnhancedForLoop> implements IStatementHandlerExtension<JCEnhancedForLoop>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCEnhancedForLoop tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		IStatementHandlerExtension<JCVariableDecl> varHandler = statementHandlerRegistry.get(Lang.C_SHARP + Kind.VARIABLE);

		languageHelper.newLineIntend();
		writer.append("foreach (");

		JCVariableDecl variable = tree.getVariable();
		varHandler.handle(variable, false);

		writer.append(" : ");
		languageHelper.writeExpressionTree(tree.getExpression());
		writer.append(")");

		StatementTree statement = tree.getStatement();
		handleChildStatement(statement);
	}
}
