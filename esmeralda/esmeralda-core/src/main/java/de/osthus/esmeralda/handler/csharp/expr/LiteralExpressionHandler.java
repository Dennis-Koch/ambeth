package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.misc.IWriter;

public class LiteralExpressionHandler extends AbstractExpressionHandler<JCExpression>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Override
	protected void handleExpressionIntern(JCExpression expression)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		if (expression instanceof JCIdent)
		{
			ILanguageHelper languageHelper = context.getLanguageHelper();
			languageHelper.writeVariableName(((JCIdent) expression).name.toString());

			Type identType = ((JCIdent) expression).type;
			if (identType == null)
			{
				String typeName = astHelper.resolveTypeFromVariableName(((JCIdent) expression).getName().toString());
				context.setTypeOnStack(typeName);
				return;
			}
			context.setTypeOnStack(identType.toString());
			return;
		}

		JCLiteral literal = (JCLiteral) expression;
		writer.append(literal.toString());

		if (literal.type != null)
		{
			context.setTypeOnStack(((JCLiteral) expression).type.toString());
			return;
		}
		switch (literal.getKind())
		{
			case BOOLEAN_LITERAL:
			{
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			case CHAR_LITERAL:
			{
				context.setTypeOnStack(char.class.getName());
				break;
			}
			case FLOAT_LITERAL:
			{
				context.setTypeOnStack(float.class.getName());
				break;
			}
			case DOUBLE_LITERAL:
			{
				context.setTypeOnStack(double.class.getName());
				break;
			}
			case INT_LITERAL:
			{
				context.setTypeOnStack(int.class.getName());
				break;
			}
			case LONG_LITERAL:
			{
				context.setTypeOnStack(long.class.getName());
				break;
			}
			case STRING_LITERAL:
			{
				context.setTypeOnStack(String.class.getName());
				break;
			}
			case NULL_LITERAL:
			{
				context.setTypeOnStack(null);
				break;
			}
			default:
				throw new RuntimeException("Kind not supported: " + literal.getKind());
		}
	}
}
