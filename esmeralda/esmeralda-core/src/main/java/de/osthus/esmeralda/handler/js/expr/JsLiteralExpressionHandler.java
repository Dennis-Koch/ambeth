package de.osthus.esmeralda.handler.js.expr;

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
			JavaClassInfo variableCI = context.lookupVariableDecl(expressionNameString);
			if (variableCI != null)
			{
				languageHelper.writeVariableNameAccess(expressionNameString);
				context.setTypeOnStack(variableCI.getFqName());
				return;
			}
			JavaClassInfo classInfo = identityExpression.type != null ? classInfoManager.resolveClassInfo(identityExpression.type.toString()) : null;
			if (classInfo != null && classInfo.isEnum())
			{
				languageHelper.writeType(classInfo.getFqName());
				writer.append('.');
				languageHelper.writeVariableName(expressionNameString);
			}
			else
			{
				languageHelper.writeVariableNameAccess(expressionNameString);
			}
			if (classInfo != null)
			{
				context.setTypeOnStack(classInfo.getFqName());
				return;
			}
			String type = astHelper.resolveTypeFromVariableName(expressionNameString);
			context.setTypeOnStack(type);
			return;
		}

		JCLiteral literal = (JCLiteral) expression;
		// To remove trailing 'F' and 'L'.
		boolean valueIsNumber = literal.value != null && literal.value instanceof Number;
		boolean valueMayContainLetter = literal.typetag == 5 || literal.typetag == 6 || literal.typetag == 7; // Long, Float, Double
		boolean useValueField = valueIsNumber && valueMayContainLetter;
		String value = useValueField ? literal.value.toString() : literal.toString();

		if (literal.typetag == 10) // String
		{
			// Only looks identical. This lets escaped characters be escaped in the generated code.
			value = value.replaceAll("\\\\", "\\\\");
		}
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
