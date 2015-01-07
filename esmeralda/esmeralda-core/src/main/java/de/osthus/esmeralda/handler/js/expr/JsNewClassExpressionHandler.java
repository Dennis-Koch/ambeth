package de.osthus.esmeralda.handler.js.expr;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class JsNewClassExpressionHandler extends AbstractExpressionHandler<JCNewClass>
{
	public static final Pattern anonymousPattern = Pattern.compile("<anonymous (.+)>([^<>]*)");

	// TODO
	public static final String getFqNameFromAnonymousName(String fqName)
	{
		Matcher anonymousMatcher = JsNewClassExpressionHandler.anonymousPattern.matcher(fqName);
		if (!anonymousMatcher.matches())
		{
			return fqName;
		}
		return anonymousMatcher.group(1) + anonymousMatcher.group(2);
	}

	public static final String findFqAnonymousName(TreePath path)
	{
		TreePath currPath = path;
		String reverseSuffix = "";
		String anonymousFqName;
		while (true)
		{
			JCClassDecl leaf = (JCClassDecl) currPath.getLeaf();
			anonymousFqName = buildGenericTypeName(leaf);
			if (anonymousFqName.indexOf('.') != -1)
			{
				return anonymousFqName + reverseSuffix;
			}
			else if (reverseSuffix.length() > 0)
			{
				reverseSuffix = "." + anonymousFqName + reverseSuffix;
			}
			else
			{
				reverseSuffix = "." + anonymousFqName;
			}
			currPath = currPath.getParentPath();
		}
	}

	public static String buildGenericTypeName(JCClassDecl classdecl)
	{
		String fqName = classdecl.sym.toString();

		StringBuilder simpleNameBuilder = new StringBuilder(fqName);
		boolean first = true;
		for (JCTypeParameter tp : classdecl.typarams)
		{
			if (first)
			{
				simpleNameBuilder.append('<');
				first = false;
			}
			else
			{
				simpleNameBuilder.append(',');
			}
			simpleNameBuilder.append(tp.type.toString());
		}
		if (!first)
		{
			simpleNameBuilder.append('>');
		}
		return simpleNameBuilder.toString();
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	protected void handleExpressionIntern(JCNewClass newClass)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		List<JCExpression> arguments = newClass.args;
		// the type can be null in the case of the internal constructor of enums
		String owner = newClass.type != null ? newClass.type.toString() : null;
		if (owner == null || "<any>".equals(owner))
		{
			owner = newClass.clazz.toString();
		}
		owner = astHelper.resolveFqTypeFromTypeName(owner);
		JCClassDecl def = newClass.def;
		if (def == null)
		{
			writer.append("new ");
			languageHelper.writeType(owner);
			languageHelper.writeMethodArguments(arguments);
			String typeOnStack = context.getClassInfo().getFqName();
			if (newClass.type != null || newClass.clazz instanceof JCIdent)
			{
				typeOnStack = owner;
			}
			context.setTypeOnStack(typeOnStack);
			return;
		}
		// this is an anonymous class instantiation
		// writeDelegate(owner, def);
		writeAnonymousInstantiation(owner, def);
	}

	protected void writeAnonymousInstantiation(String owner, JCClassDecl def)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		owner = JsNewClassExpressionHandler.getFqNameFromAnonymousName(def.sym.toString());
		JavaClassInfo newClassInfo = context.resolveClassInfo(owner);

		writer.append("new ");
		languageHelper.writeType(owner);
		writer.append('(');
		boolean firstParameter = true;
		for (IVariable usedVariable : newClassInfo.getAllUsedVariables())
		{
			firstParameter = languageHelper.writeStringIfFalse(", ", firstParameter);
			writer.append(usedVariable.getName());
		}
		writer.append(')');
		context.setTypeOnStack(owner);
	}

	protected void writeDelegate(String owner, JCClassDecl def)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		owner = getFqNameFromAnonymousName(owner);

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
			IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.getExtension(Lang.JS + parameter.getKind());
			stmtHandler.handle(parameter, false);
		}
		writer.append(')');

		IStatementHandlerExtension<JCBlock> blockHandler = statementHandlerRegistry.getExtension(Lang.JS + Kind.BLOCK);
		blockHandler.handle(delegateMethod.getBody());
		writer.append(')');

		context.setTypeOnStack(owner);
	}
}
