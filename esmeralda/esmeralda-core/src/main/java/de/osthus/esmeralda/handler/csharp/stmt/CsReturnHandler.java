package de.osthus.esmeralda.handler.csharp.stmt;

import java.util.List;

import com.sun.source.tree.TypeParameterTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCReturn;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractStatementHandler;
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
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		Method method = context.getMethod();

		languageHelper.newLineIndent();

		writer.append("return");

		final JCExpression initializer = tree.getExpression();
		if (initializer != null)
		{
			writer.append(" ");

			String returnType = method.getReturnType();
			List<TypeParameterTree> allTypeParameters = astHelper.resolveAllTypeParameters();
			if (allTypeParameters.size() > 0)
			{
				String typeOnStack = astHelper.writeToStash(new IResultingBackgroundWorkerDelegate<String>()
				{
					@Override
					public String invoke() throws Throwable
					{
						IConversionContext context = CsReturnHandler.this.context.getCurrent();
						ILanguageHelper languageHelper = context.getLanguageHelper();
						languageHelper.writeExpressionTree(initializer);
						return context.getTypeOnStack();
					}
				});
				if (!returnType.equals(typeOnStack))
				{
					for (TypeParameterTree typeParameter : allTypeParameters)
					{
						if (typeParameter.getName().contentEquals(returnType))
						{
							writer.append('(');
							writer.append(returnType);
							writer.append(')');
							break;
						}
					}
				}
			}
			languageHelper.writeExpressionTree(initializer);
		}

		writer.append(';');
	}
}
