package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class CsForHandler extends AbstractCsStatementHandler<JCForLoop> implements IStatementHandlerExtension<JCForLoop>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCForLoop tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.newLineIndent();
		writer.append("for (");

		List<JCStatement> initializer = tree.getInitializer();
		for (int i = 0, length = initializer.length(); i < length; i++)
		{
			JCStatement statement = initializer.get(i);
			if (i == 0)
			{
				handleChildStatement(statement, false);
			}
			else
			{
				JCVariableDecl var = (JCVariableDecl) statement;
				writer.append(", ");
				languageHelper.writeVariableName(var.name.toString());
				writer.append(" = ");
				JCExpression init = var.init;
				languageHelper.writeExpressionTree(init);
			}
		}

		writer.append(';');

		JCExpression condition = tree.getCondition();
		writer.append(' ');
		languageHelper.writeExpressionTree(condition);

		writer.append(';');

		List<JCExpressionStatement> update = tree.getUpdate();
		for (int i = 0, length = update.length(); i < length; i++)
		{
			JCStatement statement = update.get(i);
			writer.append(i > 0 ? ", " : " ");
			handleChildStatement(statement, false);
		}

		writer.append(')');

		StatementTree statement = tree.getStatement();
		handleChildStatement(statement);
	}
}
