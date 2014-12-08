package de.osthus.esmeralda.handler.js.expr;

import com.sun.tools.javac.tree.JCTree.JCBinary;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.ASTHelper;
import de.osthus.esmeralda.handler.csharp.expr.BinaryExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;

public class JsBinaryExpressionHandler extends BinaryExpressionHandler
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
		switch (binary.getKind())
		{
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
			default:
				super.handleExpressionIntern(binary);
		}
	}

	@Override
	protected void handleEquals(final JCBinary binary, boolean notEquals)
	{
		IConversionContext context = this.context.getCurrent();
		final ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		String[] typeOnStack = astHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String[], Object>()
		{
			@Override
			public String[] invoke(Object state) throws Throwable
			{
				IConversionContext context = JsBinaryExpressionHandler.this.context;
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
				writeSimpleBinary(" === ", binary);
			}
			else
			{
				writeSimpleBinary(" !== ", binary);
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

	@Override
	protected void writeSimpleBinary(String operator, JCBinary binary)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		languageHelper.writeExpressionTree(binary.lhs);
		writer.append(operator);
		languageHelper.writeExpressionTree(binary.rhs);
	}
}
