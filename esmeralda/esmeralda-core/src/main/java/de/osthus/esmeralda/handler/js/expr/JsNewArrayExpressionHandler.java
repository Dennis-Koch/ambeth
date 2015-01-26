package de.osthus.esmeralda.handler.js.expr;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.util.List;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;

public class JsNewArrayExpressionHandler extends AbstractExpressionHandler<JCNewArray>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCNewArray newArray)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		int dimensionCount = 0;
		Type currType = newArray.type;
		if (currType == null)
		{
			dimensionCount = newArray.dims.size();
		}
		else
		{
			while (currType instanceof ArrayType)
			{
				dimensionCount++;
				currType = ((ArrayType) currType).getComponentType();
			}
		}

		int specifiedDimensions = 0;
		for (JCExpression dimension : newArray.getDimensions())
		{
			writer.append('[');
			languageHelper.writeExpressionTree(dimension);
			writer.append(']');
			specifiedDimensions++;
		}
		List<JCExpression> initializers = newArray.getInitializers();
		if (initializers != null)
		{
			boolean firstInit = true;
			writer.append('[');
			for (JCExpression initializer : initializers)
			{
				firstInit = languageHelper.writeStringIfFalse(", ", firstInit);
				languageHelper.writeExpressionTree(initializer);
			}
			writer.append(']');
			specifiedDimensions++;
		}
		else
		{
			while (specifiedDimensions < dimensionCount)
			{
				writer.append("[]");
				specifiedDimensions++;
			}
		}
	}
}
