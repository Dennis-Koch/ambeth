package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.tree.JCTree.JCBinary;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.misc.IWriter;

public class BinaryExpressionHandler extends AbstractExpressionHandler<JCBinary>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCBinary binary)
	{
		switch (binary.getKind())
		{
			case DIVIDE:
			{
				writeSimpleBinary(" / ", binary);
				break;
			}
			case REMAINDER:
			{
				writeSimpleBinary(" % ", binary);
				break;
			}
			case LEFT_SHIFT:
			{
				writeSimpleBinary(" << ", binary);
				break;
			}
			case RIGHT_SHIFT:
			{
				writeSimpleBinary(" >> ", binary);
				break;
			}
			case MINUS:
			{
				writeSimpleBinary(" - ", binary);
				break;
			}
			case MULTIPLY:
			{
				writeSimpleBinary(" * ", binary);
				break;
			}
			case AND:
			{
				writeSimpleBinary(" & ", binary);
				break;
			}
			case OR:
			{
				writeSimpleBinary(" | ", binary);
				break;
			}
			case XOR:
			{
				writeSimpleBinary(" ^ ", binary);
				break;
			}
			case CONDITIONAL_AND:
			{
				writeSimpleBinary(" && ", binary);
				break;
			}
			case CONDITIONAL_OR:
			{
				writeSimpleBinary(" || ", binary);
				break;
			}
			case PLUS:
			{
				writeSimpleBinary(" + ", binary);
				break;
			}
			case EQUAL_TO:
			{
				writeSimpleBinary(" == ", binary);
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			case NOT_EQUAL_TO:
			{
				writeSimpleBinary(" != ", binary);
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			case GREATER_THAN:
			{
				writeSimpleBinary(" > ", binary);
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			case LESS_THAN:
			{
				writeSimpleBinary(" < ", binary);
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			case GREATER_THAN_EQUAL:
			{
				writeSimpleBinary(" >= ", binary);
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			case LESS_THAN_EQUAL:
			{
				writeSimpleBinary(" <= ", binary);
				context.setTypeOnStack(boolean.class.getName());
				break;
			}
			default:
				log.warn("Could not handle binary of type " + binary.getKind() + ": " + binary);
		}
	}

	protected void writeSimpleBinary(String operator, JCBinary binary)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		languageHelper.writeExpressionTree(binary.lhs);
		writer.append(operator);
		languageHelper.writeExpressionTree(binary.rhs);
	}
}
