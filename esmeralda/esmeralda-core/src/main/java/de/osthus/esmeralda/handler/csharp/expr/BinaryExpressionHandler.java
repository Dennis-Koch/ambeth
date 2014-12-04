package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.tree.JCTree.JCBinary;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.ASTHelper;
import de.osthus.esmeralda.misc.IWriter;

public class BinaryExpressionHandler extends AbstractExpressionHandler<JCBinary>
{
	protected static final HashSet<String> referenceEqualsTypeSet = new HashSet<String>();

	static
	{
		referenceEqualsTypeSet.addAll(ASTHelper.primitiveTypeSet);
		referenceEqualsTypeSet.add("System.Type");
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(final JCBinary binary)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
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
				handleEquals(binary, false);
				break;
			}
			case NOT_EQUAL_TO:
			{
				handleEquals(binary, true);
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
			case UNSIGNED_RIGHT_SHIFT:
			{
				astHelper.writeToStash(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						writeSimpleBinary(" >> ", binary);
					}
				});
				String typeOnStack = context.getTypeOnStack();
				if (Integer.TYPE.getName().equals(typeOnStack))
				{
					writer.append("(int)((uint)");
					writeSimpleBinary(" >> ", binary);
					writer.append(')');
				}
				else
				{
					System.out.println("ABC");
				}
				break;
			}
			default:
				log.warn("Could not handle binary of type " + binary.getKind() + ": " + binary);
		}
	}

	protected void handleEquals(final JCBinary binary, boolean notEquals)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String[] typeOnStack = astHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String[], Object>()
		{
			@Override
			public String[] invoke(Object state) throws Throwable
			{
				IConversionContext context = BinaryExpressionHandler.this.context;
				String[] resultTypes = new String[2];
				languageHelper.writeExpressionTree(binary.lhs);
				resultTypes[0] = context.getTypeOnStack();
				languageHelper.writeExpressionTree(binary.rhs);
				resultTypes[1] = context.getTypeOnStack();
				return resultTypes;
			}
		}, null);

		if (typeOnStack[0] == null || typeOnStack[1] == null || referenceEqualsTypeSet.contains(typeOnStack[0])
				|| referenceEqualsTypeSet.contains(typeOnStack[1]))
		{
			if (!notEquals)
			{
				writeSimpleBinary(" != ", binary);
			}
			else
			{
				writeSimpleBinary(" == ", binary);
			}
		}
		else
		{
			if (notEquals)
			{
				writer.append('!');
			}
			languageHelper.writeTypeDirect("System.Object");
			writer.append(".ReferenceEquals(");
			languageHelper.writeExpressionTree(binary.lhs);
			writer.append(", ");
			languageHelper.writeExpressionTree(binary.rhs);
			writer.append(");");
		}
		context.setTypeOnStack(boolean.class.getName());
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
