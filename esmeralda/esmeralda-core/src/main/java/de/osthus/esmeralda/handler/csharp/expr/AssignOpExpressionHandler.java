package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.tree.JCTree.JCAssignOp;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.misc.IWriter;

public class AssignOpExpressionHandler extends AbstractExpressionHandler<JCAssignOp>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCAssignOp assignOp)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		switch (assignOp.getKind())
		{
			case AND_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" &= ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case DIVIDE_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" /= ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case LEFT_SHIFT_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" <<= ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case MINUS_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" -= ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case MULTIPLY_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" *= ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case OR_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" |= ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case PLUS_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" += ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case RIGHT_SHIFT_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" >>= ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case XOR_ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" ^= ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			case ASSIGNMENT:
			{
				languageHelper.writeExpressionTree(assignOp.lhs);
				writer.append(" = ");
				languageHelper.writeExpressionTree(assignOp.rhs);
				break;
			}
			default:
				log.warn("Could not handle assignment of type " + assignOp.getKind() + ": " + assignOp);
		}
		context.setTypeOnStack(null);
	}
}
