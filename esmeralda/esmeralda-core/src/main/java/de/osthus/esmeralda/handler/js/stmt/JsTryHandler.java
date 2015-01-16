package de.osthus.esmeralda.handler.js.stmt;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.uni.stmt.UniversalBlockHandler;
import de.osthus.esmeralda.misc.IWriter;

public class JsTryHandler extends AbstractJsStatementHandler<JCTry> implements IStatementHandlerExtension<JCTry>
{
	public static final Pattern RUNTIME_EXCEPTION_UTIL = Pattern.compile("\\s*throw\\s+RuntimeExceptionUtil\\.mask\\(\\s*(\\S+)\\s*\\)\\s*;\\s*");

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

		final List<JCCatch> catches = tryStatement.getCatches();

		JCBlock finallyBlock = tryStatement.getFinallyBlock();
		if (finallyBlock == null || finallyBlock.getStatements().size() == 0)
		{
			if (catches.size() == 0)
			{
				// we have no finally block (or at least none with a non-zero content) and no catch block remaining. so the whole try statement including the
				// internal block is obsolete now
				blockHandler.writeBlockContentWithoutIntendation(tryStatement.getBlock());
				return;
			}
		}

		languageHelper.newLineIndent();
		writer.append("try ");
		handleChildStatement(tryStatement.getBlock());

		writer.append(" catch (e) ");
		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				writeCatch(catches);
			}
		});

		if (finallyBlock != null)
		{
			writer.append(" finally ");
			handleChildStatement(tryStatement.getFinallyBlock());
		}
	}

	protected void writeCatch(List<JCCatch> catches)
	{
		IConversionContext context = this.context.getCurrent();
		final ILanguageHelper languageHelper = context.getLanguageHelper();
		final IWriter writer = context.getWriter();

		languageHelper.newLineIndent();

		boolean firstCatch = true;
		for (JCCatch catchStatement : catches)
		{
			JCVariableDecl parameter = catchStatement.getParameter();

			firstCatch = languageHelper.writeStringIfFalse(" else ", firstCatch);
			writer.append("if (Ambeth.instanceOf(e, \"");
			languageHelper.writeType(parameter.vartype.toString());
			writer.append("\")) ");

			JCBlock blockOfCatchBlock = catchStatement.getBlock();
			if (blockOfCatchBlock.getStatements().size() == 1)
			{
				String stringOfStatement = blockOfCatchBlock.getStatements().get(0).toString();
				Matcher redundantCatchMatcher = RUNTIME_EXCEPTION_UTIL.matcher(stringOfStatement);
				if (redundantCatchMatcher.matches())
				{
					languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
					{
						@Override
						public void invoke() throws Throwable
						{
							languageHelper.newLineIndent();
							writer.append("throw e;");
						}
					});
					continue;
				}
			}

			handleChildStatement(catchStatement.getBlock());
		}
	}
}
