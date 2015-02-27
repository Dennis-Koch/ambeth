package de.osthus.esmeralda.handler.js.expr;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IVariable;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.js.IMethodParamNameService;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.SnippetTrigger;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class JsNewClassExpressionHandler extends AbstractExpressionHandler<JCNewClass>
{
	public static final Pattern anonymousPattern = Pattern.compile("<anonymous (.+)>([^<>]*)");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IMethodParamNameService methodParamNameService;

	@Override
	protected void handleExpressionIntern(JCNewClass newClass)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();

		// the type can be null in the case of the internal constructor of enums
		String owner = newClass.type != null ? newClass.type.toString() : null;
		if (owner == null || "<any>".equals(owner))
		{
			owner = newClass.clazz.toString();
		}
		JCClassDecl def = newClass.def;
		if (def == null)
		{
			writer.append("Ext.create(\"");
			languageHelper.writeType(owner);
			writer.append('"');
			List<JCExpression> arguments = newClass.args;
			if (!arguments.isEmpty())
			{
				Iterator<String> paramNames = extractParamNames(newClass);
				writer.append(", { ");
				boolean firstParameter = true;
				for (JCExpression argument : arguments)
				{
					firstParameter = languageHelper.writeStringIfFalse(", ", firstParameter);
					String paramName = paramNames.next();
					writer.append('"').append(paramName).append("\" : ");
					languageHelper.writeExpressionTree(argument);
				}
				writer.append(" }");
			}
			writer.append(')');
			String typeOnStack = context.getClassInfo().getFqName();
			if (newClass.type != null || newClass.clazz instanceof JCIdent)
			{
				typeOnStack = owner;
			}
			context.setTypeOnStack(typeOnStack);
			return;
		}
		// this is an anonymous class instantiation
		writeAnonymousInstantiation(owner, def);
	}

	protected Iterator<String> extractParamNames(JCNewClass newClass)
	{
		IConversionContext context = this.context.getCurrent();
		final IJsHelper languageHelper = (IJsHelper) context.getLanguageHelper();

		MethodSymbol constructor = (MethodSymbol) newClass.constructor;
		ArrayList<String> paramNames = new ArrayList<>();
		if (constructor != null && constructor.params != null)
		{
			for (VarSymbol param : constructor.params)
			{
				paramNames.add(param.name.toString());
			}
		}
		else
		{
			if (constructor != null && constructor.type != null)
			{
				String fqClassName = newClass.type.toString();
				fqClassName = languageHelper.removeGenerics(fqClassName);

				com.sun.tools.javac.util.List<Type> argtypes = ((MethodType) constructor.type).argtypes;
				int size = argtypes.size();
				String[] fqParamClassNames = new String[size];
				for (int i = 0; i < size; i++)
				{
					Type argType = argtypes.get(i);
					fqParamClassNames[i] = languageHelper.removeGenerics(argType.toString());
				}

				String[] paramNamesArray = methodParamNameService.getConstructorParamNames(fqClassName, fqParamClassNames);
				if (paramNamesArray != null)
				{
					paramNames.addAll(paramNamesArray);
				}
				else
				{
					String newClassString = newClass.toString();
					throw new SnippetTrigger("No names for called constructors parameters available").setContext(newClassString);
				}
			}
			else
			{
				String className = newClass.clazz.toString();
				JavaClassInfo classInfo = context.resolveClassInfo(className, true);
				if (classInfo == null)
				{
					String newClassString = newClass.toString();
					throw new SnippetTrigger("No names or types for called constructors parameters available").setContext(newClassString);
				}
				String fqClassName = classInfo.getFqName();
				fqClassName = languageHelper.removeGenerics(fqClassName);

				com.sun.tools.javac.util.List<JCExpression> args = newClass.args;
				int size = args.size();
				String[] fqParamClassNames = new String[size];
				for (int i = 0; i < size; i++)
				{
					final JCExpression param = args.get(i);
					if (param instanceof JCLiteral)
					{
						Object value = ((JCLiteral) param).value;
						fqParamClassNames[i] = languageHelper.removeGenerics(value.getClass().getName());
					}
					else
					{
						String typeOnStack = astHelper.writeToStash(new IResultingBackgroundWorkerDelegate<String>()
						{
							@Override
							public String invoke() throws Throwable
							{
								IConversionContext context = JsNewClassExpressionHandler.this.context;
								languageHelper.writeExpressionTree(param);
								String resultType = context.getTypeOnStack();
								return resultType;
							}
						});
						JavaClassInfo onStackClassInfo = context.resolveClassInfo(typeOnStack, true);
						if (classInfo != null)
						{
							typeOnStack = onStackClassInfo.getFqName();
						}
						fqParamClassNames[i] = languageHelper.removeGenerics(typeOnStack);
					}
					if (fqParamClassNames[i] == null)
					{
						String newClassString = newClass.toString();
						throw new SnippetTrigger("No names or types for called constructors parameters available").setContext(newClassString);
					}
				}

				String[] paramNamesArray = methodParamNameService.getConstructorParamNames(fqClassName, fqParamClassNames);
				if (paramNamesArray != null)
				{
					paramNames.addAll(paramNamesArray);
				}
				else
				{
					String newClassString = newClass.toString();
					throw new SnippetTrigger("No names or types for called constructors parameters available").setContext(newClassString);
				}
			}
		}
		return paramNames.iterator();
	}

	protected void writeAnonymousInstantiation(String owner, JCClassDecl def)
	{
		IConversionContext context = this.context.getCurrent();
		IJsHelper languageHelper = (IJsHelper) context.getLanguageHelper();
		IWriter writer = context.getWriter();
		HashSet<String> methodScopeVars = languageHelper.getLanguageSpecific().getMethodScopeVars();

		owner = getFqNameFromAnonymousName(def.sym.toString());
		JavaClassInfo newClassInfo = context.resolveClassInfo(owner);

		writer.append("Ext.create(\"");
		languageHelper.writeType(owner);
		writer.append("\", { ");

		HashSet<String> alreadyHandled = new HashSet<>();
		boolean firstParameter = true;
		for (IVariable usedVariable : newClassInfo.getAllUsedVariables())
		{
			String name = usedVariable.getName();
			if (!alreadyHandled.add(name))
			{
				// The IVariable instances have no equals(). So there are duplicates.
				continue;
			}

			firstParameter = languageHelper.writeStringIfFalse(", ", firstParameter);
			writer.append('"').append(name).append("\" : ");
			if (!methodScopeVars.contains(name))
			{
				writer.append("this.");
			}
			writer.append(name);
		}
		writer.append(" })");
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

	protected String getFqNameFromAnonymousName(String fqName)
	{
		Matcher anonymousMatcher = JsNewClassExpressionHandler.anonymousPattern.matcher(fqName);
		if (!anonymousMatcher.matches())
		{
			return fqName;
		}
		return anonymousMatcher.group(1) + anonymousMatcher.group(2);
	}

	protected String findFqAnonymousName(TreePath path)
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

	protected String buildGenericTypeName(JCClassDecl classdecl)
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
}
