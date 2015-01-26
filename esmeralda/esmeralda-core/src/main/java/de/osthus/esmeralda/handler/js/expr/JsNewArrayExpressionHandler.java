package de.osthus.esmeralda.handler.js.expr;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ArrayType;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.util.List;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.misc.IToDoWriter;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class JsNewArrayExpressionHandler extends AbstractExpressionHandler<JCNewArray>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IToDoWriter toDoWriter;

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
			boolean oneNumberOnly = true;
			writer.append('[');
			for (JCExpression initializer : initializers)
			{
				oneNumberOnly = firstInit;
				firstInit = languageHelper.writeStringIfFalse(", ", firstInit);
				languageHelper.writeExpressionTree(initializer);
				if (oneNumberOnly)
				{
					String typeOnStack = context.getTypeOnStack();
					oneNumberOnly = astHelper.isNumber(typeOnStack);
				}
			}
			writer.append(']');
			specifiedDimensions++;

			if (oneNumberOnly)
			{
				Method method = context.getMethod();
				if (method != null)
				{
					toDoWriter.write("Only one Number in Array initialization", method, newArray.pos);
				}
				else
				{
					JavaClassInfo classInfo = context.getClassInfo();
					toDoWriter.write("Only one Number in Array initialization", classInfo, newArray.pos);
				}
			}
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
