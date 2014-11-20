package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;

public class CsTryHandler extends AbstractStatementHandler<JCTry> implements IStatementHandlerExtension<JCTry>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCTry tryStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();
		writer.append("try");
		handleChildStatement(tryStatement.getBlock());
		for (JCCatch catchStatement : tryStatement.getCatches())
		{
			JCVariableDecl parameter = catchStatement.getParameter();

			writer.append("catch (");
			IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.get(Lang.C_SHARP + parameter.getKind());
			stmtHandler.handle(parameter, false);
			writer.append(')');
			handleChildStatement(catchStatement.getBlock());
		}
		if (tryStatement.getFinallyBlock() != null)
		{
			writer.append("finally");
			handleChildStatement(tryStatement.getFinallyBlock());
		}
	}
}
