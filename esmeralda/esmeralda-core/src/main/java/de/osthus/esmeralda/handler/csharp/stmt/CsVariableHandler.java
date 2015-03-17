package de.osthus.esmeralda.handler.csharp.stmt;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class CsVariableHandler extends AbstractCsStatementHandler<JCVariableDecl> implements IStatementHandlerExtension<JCVariableDecl>
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

		String variableName = variableStatement.getName().toString();
		JavaClassInfo variableType = classInfoManager.resolveClassInfo(variableStatement.type.toString());
		context.pushVariableDecl(variableName, variableType);

		if (standalone)
		{
			languageHelper.newLineIndent();
		}
		languageHelper.writeType(variableType.getFqName());
		writer.append(' ');
		languageHelper.writeVariableName(variableName);

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
