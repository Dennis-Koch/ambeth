package de.osthus.esmeralda.handler.csharp.expr;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IUsedVariableDelegate;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class NewClassExpressionHandler extends AbstractExpressionHandler<JCNewClass>
{
	public static final Pattern anonymousPattern = Pattern.compile("<anonymous (.+)>([^<>]*)");

	public static final String getFqNameFromAnonymousName(String fqName)
	{
		Matcher anonymousMatcher = NewClassExpressionHandler.anonymousPattern.matcher(fqName);
		if (!anonymousMatcher.matches())
		{
			return fqName;
		}
		return anonymousMatcher.group(1) + anonymousMatcher.group(2);
	}

	public static final String getFqName(JCClassDecl classTree)
	{
		StringBuilder sb = new StringBuilder(classTree.sym.toString());
		boolean first = true;
		for (JCTypeParameter param : classTree.typarams)
		{
			if (first)
			{
				first = false;
				sb.append("<");
			}
			else
			{
				sb.append(',');
			}
			sb.append(param.toString());
		}
		if (!first)
		{
			sb.append('>');
		}
		String name = getFqNameFromAnonymousName(sb.toString());
		sb.setLength(0);
		Tree extendsClause = classTree.getExtendsClause();
		if (!(extendsClause instanceof JCTypeApply) || !classTree.getSimpleName().contentEquals(""))
		{
			return name;
		}
		sb.append(name);
		ClassType classType = (ClassType) ((JCTypeApply) extendsClause).type;
		first = true;
		for (Type param : classType.typarams_field)
		{
			if (first)
			{
				first = false;
				sb.append("<");
			}
			else
			{
				sb.append(',');
			}
			sb.append(param.toString());
		}
		if (!first)
		{
			sb.append('>');
		}
		return sb.toString();
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
		// the type can be null in the case of the internal constructor of enums
		String owner = newClass.type != null ? newClass.type.toString() : null;
		if (owner == null || "<any>".equals(owner))
		{
			owner = newClass.clazz.toString();
		}
		owner = astHelper.resolveFqTypeFromTypeName(owner);
		JCClassDecl def = newClass.def;
		boolean isAnonymousClass = def != null;
		if (!isAnonymousClass)
		{
			writeNormalInstantiation(newClass, owner);
			return;
		}
		else
		{
			// this is an anonymous class instantiation
			// writeDelegate(owner, def);
			writeAnonymousInstantiation(owner, def);
		}
	}

	protected void writeNormalInstantiation(JCNewClass newClass, String owner)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		List<JCExpression> arguments = newClass.args;

		writer.append("new ");
		languageHelper.writeType(owner);
		languageHelper.writeMethodArguments(arguments);
		String typeOnStack = context.getClassInfo().getFqName();
		if (newClass.type != null || newClass.clazz instanceof JCIdent)
		{
			typeOnStack = owner;
		}
		context.setTypeOnStack(typeOnStack);
	}

	protected void writeAnonymousInstantiation(String owner, JCClassDecl def)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		owner = NewClassExpressionHandler.getFqName(def);
		JavaClassInfo newClassInfo = classInfoManager.resolveClassInfo(owner);

		writer.append("new ");
		languageHelper.writeType(owner);
		writer.append('(');
		languageHelper.forAllUsedVariables(newClassInfo, new IUsedVariableDelegate()
		{
			@Override
			public void invoke(IVariable usedVariable, boolean firstVariable, IConversionContext context, ILanguageHelper languageHelper, IWriter writer)
			{
				languageHelper.writeStringIfFalse(", ", firstVariable);
				writer.append(usedVariable.getName());
			}
		});
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
			IStatementHandlerExtension<StatementTree> stmtHandler = statementHandlerRegistry.getExtension(Lang.C_SHARP + parameter.getKind());
			stmtHandler.handle(parameter, false);
		}
		writer.append(')');

		IStatementHandlerExtension<JCBlock> blockHandler = statementHandlerRegistry.getExtension(Lang.C_SHARP + Kind.BLOCK);
		blockHandler.handle(delegateMethod.getBody());
		writer.append(')');

		context.setTypeOnStack(owner);
	}
}
