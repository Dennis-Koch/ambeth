package de.osthus.esmeralda.handler.csharp.expr;

import com.sun.tools.javac.tree.JCTree.JCTypeCast;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class TypeCastExpressionHandler extends AbstractExpressionHandler<JCTypeCast>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCTypeCast expression)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		JavaClassInfo classInfo = classInfoManager.resolveClassInfo(expression.clazz.toString());

		writer.append("(");
		languageHelper.writeType(classInfo.getFqName());
		writer.append(")");

		if (classInfo.getName().equals(classInfo.getNonGenericName()))
		{
			languageHelper.writeExpressionTree(expression.expr);
		}
		else
		{
			Object pushResult = context.pushTypeErasureHint(classInfo);
			try
			{
				languageHelper.writeExpressionTree(expression.expr);
			}
			finally
			{
				context.popTypeErasureHint(pushResult);
			}
		}
		context.setTypeOnStack(classInfo.getFqName());
	}
}
