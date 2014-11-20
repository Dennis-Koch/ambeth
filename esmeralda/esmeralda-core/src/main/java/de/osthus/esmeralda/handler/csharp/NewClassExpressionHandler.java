package de.osthus.esmeralda.handler.csharp;

import java.util.List;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;

public class NewClassExpressionHandler extends AbstractExpressionHandler<JCNewClass>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCNewClass newClass)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		List<JCExpression> arguments = newClass.args;
		List<Type> genericTypeArguments = newClass.type != null ? newClass.type.allparams() : null;
		// List<Type> argumentTypes = ((MethodType) newClass.constructor.type).getTypeArguments();
		String owner = newClass.constructor != null ? ((ClassSymbol) newClass.constructor.owner).fullname.toString() : newClass.clazz.toString();

		writer.append("new ");
		languageHelper.writeType(owner);

		languageHelper.writeGenericTypeArguments(genericTypeArguments);
		languageHelper.writeMethodArguments(arguments);
	}
}
