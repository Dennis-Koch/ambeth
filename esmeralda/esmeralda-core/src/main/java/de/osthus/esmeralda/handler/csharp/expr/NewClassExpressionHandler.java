package de.osthus.esmeralda.handler.csharp.expr;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;

public class NewClassExpressionHandler extends AbstractExpressionHandler<JCNewClass>
{
	public static final Pattern anonymousPattern = Pattern.compile("<anonymous (.+)>");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCNewClass newClass)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		List<JCExpression> arguments = newClass.args;
		// the type can be null in the case of the internal constructor of enums
		String owner = newClass.type != null ? newClass.type.toString() : newClass.clazz.toString();
		JCClassDecl def = newClass.def;
		if (def == null)
		{
			writer.append("new ");
			languageHelper.writeType(owner);
			languageHelper.writeMethodArguments(arguments);
			context.setTypeOnStack(newClass.type != null ? owner : context.getClassInfo().getPackageName() + "." + context.getClassInfo().getName());
			return;
		}
		// this is an anonymous class instantiation
		Matcher anonymousMatcher = anonymousPattern.matcher(owner);
		if (!anonymousMatcher.matches())
		{
			throw new IllegalStateException(owner);
		}
		owner = anonymousMatcher.group(1);

		if (def.defs.size() != 2)
		{
			// 1 method is always the constructor, the other method the delegate method
			throw new IllegalStateException("Anonymous class must define exactly one method to be able to be converted to a C# delegate");
		}
		JCMethodDecl delegateMethod = null;
		for (JCTree method : def.defs)
		{
			if (!"<init>".equals(((JCMethodDecl) method).getName().toString()))
			{
				delegateMethod = (JCMethodDecl) method;
				break;
			}
		}
		writer.append("new ");
		languageHelper.writeType(owner);
		writer.append("(delegate(");
		boolean firstParameter = true;

		for (JCVariableDecl parameter : delegateMethod.getParameters())
		{
			firstParameter = languageHelper.writeStringIfFalse(", ", firstParameter);
			IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.get(Lang.C_SHARP + parameter.getKind());
			stmtHandler.handle(parameter, false);
		}
		writer.append(')');

		IStatementHandlerExtension<JCBlock> blockHandler = statementHandlerRegistry.get(Lang.C_SHARP + Kind.BLOCK);
		blockHandler.handle(delegateMethod.getBody());
		writer.append(')');

		context.setTypeOnStack(owner);
	}
}
