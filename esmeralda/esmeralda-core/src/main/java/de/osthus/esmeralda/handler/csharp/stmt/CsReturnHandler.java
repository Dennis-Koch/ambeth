package de.osthus.esmeralda.handler.csharp.stmt;

import java.util.List;

import com.sun.source.tree.TypeParameterTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCReturn;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Method;

public class CsReturnHandler extends AbstractStatementHandler<JCReturn> implements IStatementHandlerExtension<JCReturn>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void handle(JCReturn tree, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		Method method = context.getMethod();

		languageHelper.newLineIntend();

		writer.append("return");

		JCExpression initializer = tree.getExpression();
		if (initializer != null)
		{
			writer.append(" ");

			String returnType = method.getReturnType();
			List<TypeParameterTree> allTypeParameters = astHelper.resolveAllTypeParameters();
			for (TypeParameterTree typeParameter : allTypeParameters)
			{
				if (!typeParameter.getName().contentEquals(returnType))
				{
					continue;
				}
				writer.append('(');
				writer.append(returnType);
				writer.append(')');
			}
			languageHelper.writeExpressionTree(initializer);
		}

		writer.append(';');
	}
}
