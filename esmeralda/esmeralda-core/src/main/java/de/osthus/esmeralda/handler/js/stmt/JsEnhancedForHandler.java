package de.osthus.esmeralda.handler.js.stmt;

import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class JsEnhancedForHandler extends AbstractJsStatementHandler<JCEnhancedForLoop> implements IStatementHandlerExtension<JCEnhancedForLoop>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCEnhancedForLoop tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		JCVariableDecl variable = tree.getVariable();
		JCExpression expression = tree.getExpression();

		languageHelper.newLineIndent();
		writer.append("// Java: ").append("for (").append(variable.toString()).append(" : ").append(expression.toString()).append(") { ... }");
		languageHelper.newLineIndent();

		if (expression.type.getClass().equals(ArrayType.class))
		{
			// Unique names even in nested loops
			String forIndexName = "forIndex_" + context.getIndentationLevel();
			String forLengthName = "forLength_" + context.getIndentationLevel();

			writer.append("for (var ").append(forIndexName).append(" = 0, ").append(forLengthName).append(" = ");
			languageHelper.writeExpressionTree(expression);
			writer.append(".length, ");
			languageHelper.writeVariableName(variable.name.toString());
			writer.append("; ").append(forIndexName).append(" < ").append(forLengthName).append("; ");
			languageHelper.writeVariableName(variable.name.toString());
			writer.append(" = ");
			languageHelper.writeExpressionTree(expression);
			writer.append("[").append(forIndexName).append("++]) ");
		}
		else
		{
			// Unique name even in nested loops
			String forIterName = "forIter_" + context.getIndentationLevel();

			writer.append("for (var ").append(forIterName).append(" = ");
			languageHelper.writeExpressionTree(expression);
			writer.append(".iterator(), ");
			languageHelper.writeVariableName(variable.name.toString());
			writer.append("; ").append(forIterName).append(".hasNext(); ");
			languageHelper.writeVariableName(variable.name.toString());
			writer.append(" = ").append(forIterName).append(".next()) ");
		}

		StatementTree statement = tree.getStatement();
		handleChildStatement(statement);
	}
}
