package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree.JCUnary;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.misc.IWriter;

public class UnaryExpressionHandler extends AbstractExpressionHandler<JCUnary>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCUnary unary)
	{
		Kind kind = unary.getKind();
		switch (kind)
		{
			case BITWISE_COMPLEMENT:
			{
				writeSimpleUnary("~", unary);
				break;
			}
			case PREFIX_DECREMENT:
			{
				writeSimpleUnary("--", unary);
				break;
			}
			case PREFIX_INCREMENT:
			{
				writeSimpleUnary("++", unary);
				break;
			}
			case POSTFIX_DECREMENT:
			{
				writeSimpleUnary(unary, "--");
				break;
			}
			case POSTFIX_INCREMENT:
			{
				writeSimpleUnary(unary, "++");
				break;
			}
			case LOGICAL_COMPLEMENT:
			{
				writeSimpleUnary("!", unary);
				break;
			}
			default:
				log.warn("Could not handle unary of type " + unary.getKind() + ": " + unary);
		}
	}

	protected void writeSimpleUnary(String prefix, JCUnary binary)
	{
		writeSimpleUnary(prefix, binary, "");
	}

	protected void writeSimpleUnary(JCUnary binary, String postfix)
	{
		writeSimpleUnary("", binary, postfix);
	}

	protected void writeSimpleUnary(String prefix, JCUnary binary, String postfix)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		writer.append(prefix);
		languageHelper.writeExpressionTree(binary.arg);
		writer.append(postfix);
	}
}
