package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewArray;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.misc.IWriter;

public class NewArrayExpressionHandler extends AbstractExpressionHandler<JCNewArray>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCNewArray newArray)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		int dimensionCount = 0;
		Type currType = newArray.type;
		while (currType instanceof ArrayType)
		{
			dimensionCount++;
			currType = ((ArrayType) currType).getComponentType();
		}
		writer.append("new ");
		languageHelper.writeType(currType.toString());
		int specifiedDimensions = 0;
		for (JCExpression dimension : newArray.getDimensions())
		{
			writer.append('[');
			languageHelper.writeExpressionTree(dimension);
			writer.append(']');
			specifiedDimensions++;
		}
		while (specifiedDimensions < dimensionCount)
		{
			writer.append("[]");
			specifiedDimensions++;
		}
		if (newArray.getInitializers() != null)
		{
			writer.append(" { ");
			boolean firstInitializer = true;
			for (JCExpression initializer : newArray.getInitializers())
			{
				firstInitializer = languageHelper.writeStringIfFalse(", ", firstInitializer);
				languageHelper.writeExpressionTree(initializer);
			}
			writer.append(" }");
		}
		context.setTypeOnStack(newArray.type.toString());
	}
}
