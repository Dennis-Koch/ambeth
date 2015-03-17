package de.osthus.esmeralda.handler.csharp.stmt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.uni.stmt.UniversalBlockHandler;
import de.osthus.esmeralda.misc.IWriter;

public class CsTryHandler extends AbstractCsStatementHandler<JCTry> implements IStatementHandlerExtension<JCTry>
{
	public static final Pattern redundantCatchPattern = Pattern.compile("\\s*throw\\s+RuntimeExceptionUtil\\.mask\\(\\s*(\\S+)\\s*\\)\\s*;\\s*");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected UniversalBlockHandler blockHandler;

	@Override
	public void handle(JCTry tryStatement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		ArrayList<JCCatch> catches = new ArrayList<JCCatch>(tryStatement.getCatches());
		JCBlock finallyBlock = tryStatement.getFinallyBlock();
		if (finallyBlock == null || finallyBlock.getStatements().size() == 0)
		{
			for (int a = catches.size(); a-- > 0;)
			{
				JCCatch catchBlock = catches.get(a);
				JCBlock blockOfCatchBlock = catchBlock.getBlock();
				if (blockOfCatchBlock.getStatements().size() != 1)
				{
					continue;
				}
				String stringOfStatement = blockOfCatchBlock.getStatements().get(0).toString();
				Matcher redundantCatchMatcher = redundantCatchPattern.matcher(stringOfStatement);
				if (!redundantCatchMatcher.matches())
				{
					continue;
				}
				if (!redundantCatchMatcher.group(1).contentEquals(catchBlock.getParameter().getName()))
				{
					continue;
				}
				catches.removeAtIndex(a);
			}
			if (catches.size() == 0)
			{
				// we have no finally block (or at least none with a non-zero content) and no catch block remaining. so the whole try statement including the
				// internal block is obsolete now
				blockHandler.writeBlockContentWithoutIntendation(tryStatement.getBlock());
				return;
			}
		}
		languageHelper.newLineIndent();
		writer.append("try");
		handleChildStatement(tryStatement.getBlock());
		for (JCCatch catchStatement : catches)
		{
			context.pushVariableDeclBlock();
			try
			{
				JCVariableDecl parameter = catchStatement.getParameter();
				languageHelper.newLineIndent();
				writer.append("catch (");
				IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.getExtension(context.getLanguage() + parameter.getKind());
				stmtHandler.handle(parameter, false);
				writer.append(')');
				handleChildStatement(catchStatement.getBlock());
			}
			finally
			{
				context.popVariableDeclBlock();
			}
		}
		if (finallyBlock != null)
		{
			languageHelper.newLineIndent();
			writer.append("finally");

			handleChildStatement(tryStatement.getFinallyBlock());
		}
	}
}
