package de.osthus.esmeralda.handler.js.expr;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class JsLiteralExpressionHandler extends AbstractExpressionHandler<JCExpression>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IJsHelper languageHelper;

	@Override
	protected void handleExpressionIntern(JCExpression expression)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		if (expression instanceof JCIdent)
		{
			JCIdent identityExpression = (JCIdent) expression;

			String expressionNameString = identityExpression.name.toString();
			if (identityExpression.sym instanceof VarSymbol && !"this".equals(expressionNameString))
			{
				JavaClassInfo classInfo = context.getClassInfo();
				Symbol owner = identityExpression.sym.owner;
				if (owner instanceof ClassSymbol)
				{
					String genericsFreeName = languageHelper.removeGenerics(classInfo.getFqName());
					boolean isOwnerCurrentClass = genericsFreeName.equals(owner.toString());
					if (isOwnerCurrentClass)
					{
						writer.append("this.");
					}
				}
			}
			languageHelper.writeVariableName(expressionNameString);

			Type identType = identityExpression.type;
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
		String value = literal.value != null ? literal.value.toString() : literal.toString();
		writer.append(value);

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
