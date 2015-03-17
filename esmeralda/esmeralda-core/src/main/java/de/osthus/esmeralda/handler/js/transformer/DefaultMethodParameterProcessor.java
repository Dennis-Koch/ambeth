package de.osthus.esmeralda.handler.js.transformer;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.VariableElement;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.esmeralda.IClassInfoManager;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.js.IJsOverloadManager;
import de.osthus.esmeralda.handler.js.IMethodParamNameService;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.snippet.SnippetTrigger;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class DefaultMethodParameterProcessor implements IMethodParameterProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IClassInfoManager classInfoManager;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IJsHelper languageHelper;

	@Autowired(IJsOverloadManager.STATIC)
	protected IJsOverloadManager overloadManagerStatic;

	@Autowired(IJsOverloadManager.NON_STATIC)
	protected IJsOverloadManager overloadManagerNonStatic;

	@Autowired
	protected IMethodParamNameService methodParamNameService;

	@Override
	public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod, IOwnerWriter ownerWriter)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		List<JCExpression> arguments = methodInvocation.getArguments();

		boolean isSuperCall = false;

		if (owner != null)
		{
			if ("this".equals(owner))
			{
				// Nothing to do. "this." is written automatically elsewhere.
			}
			else if ("super".equals(owner))
			{
				writer.append("this.");
				owner = "superclass";
				isSuperCall = true;
			}
			else if (methodInvocation.meth instanceof JCFieldAccess)
			{
				JCExpression selected = ((JCFieldAccess) methodInvocation.meth).selected;
				if (selected != null && selected instanceof JCIdent)
				{
					Symbol sym = ((JCIdent) selected).sym;
					if (sym != null && sym instanceof VarSymbol)
					{
						String varOwner = ((VarSymbol) sym).owner.toString();
						varOwner = languageHelper.removeGenerics(varOwner);
					}
				}
			}
			ownerWriter.writeOwner(owner);
			writer.append('.');
		}
		else
		{
			String methodOwner = languageHelper.removeGenerics(transformedMethod.getOwner());
			writeThisIfLocalField(methodOwner, context);
		}
		String methodName = transformedMethod.getName();

		if (!transformedMethod.isPropertyInvocation())
		{
			boolean isSuperConstructor = DefaultMethodTransformer.SUPER.equals(methodName);
			isSuperCall |= isSuperConstructor;
			String trueMethodName = methodName;
			if (overloadManagerNonStatic.hasOverloads(transformedMethod) || overloadManagerStatic.hasOverloads(transformedMethod))
			{
				IList<String> paramsList = getParamsList(methodInvocation);
				String overloadedMethodNamePostfix;
				if (paramsList != null)
				{
					overloadedMethodNamePostfix = languageHelper.createOverloadedMethodNamePostfix(paramsList);
				}
				else
				{
					throw new SnippetTrigger("No names or types for called methods parameters available").setContext(methodInvocation.toString());
				}

				if (!isSuperConstructor)
				{
					trueMethodName += overloadedMethodNamePostfix;
				}
				else
				{
					trueMethodName = trueMethodName.replace(DefaultMethodTransformer.THIS, DefaultMethodTransformer.THIS + overloadedMethodNamePostfix);
				}
			}
			writer.append(trueMethodName);
			boolean isFirstArgument = true;
			if (!isSuperCall)
			{
				writer.append('(');
			}
			else
			{
				writer.append(".call(this");
				isFirstArgument = false;
			}

			for (int a = 0, size = arguments.size(); a < size; a++)
			{
				JCExpression arg = arguments.get(a);
				if (!isFirstArgument)
				{
					writer.append(", ");
				}
				languageHelper.writeExpressionTree(arg);
				isFirstArgument = false;
			}
			writer.append(')');
		}
		else if (arguments.size() == 1)
		{
			writer.append(methodName).append(" = ");
			JCExpression argument = arguments.get(0);
			languageHelper.writeExpressionTree(argument);
			writer.append(';');
		}
		else if (arguments.size() > 0)
		{
			throw new IllegalStateException("Property assignment with multiple values not supported: " + methodInvocation);
		}
		else
		{
			writer.append(methodName);
		}
	}

	protected void writeThisIfLocalField(String ownerName, IConversionContext context)
	{
		JavaClassInfo classInfo = context.getClassInfo();
		JavaClassInfo owner = languageHelper.findClassInHierarchy(ownerName, classInfo);
		if (owner != null)
		{
			IWriter writer = context.getWriter();
			writer.append("this.");
		}
	}

	protected IList<String> getParamsList(final JCMethodInvocation methodInvocation)
	{
		if (methodInvocation.meth == null && methodInvocation.args == null)
		{
			return null;
		}

		IList<String> paramsList = new ArrayList<>();

		boolean processed = false;
		String methodName = null;

		if (methodInvocation.meth instanceof JCIdent)
		{
			JCIdent meth = (JCIdent) methodInvocation.meth;
			if (meth.sym != null)
			{
				getParamsList(paramsList, (MethodSymbol) meth.sym);
				processed = true;
			}
			else
			{
				methodName = meth.name.toString();
			}
		}
		else if (methodInvocation.meth instanceof JCFieldAccess)
		{
			JCFieldAccess meth = (JCFieldAccess) methodInvocation.meth;
			if (meth.sym != null)
			{
				getParamsList(paramsList, (MethodSymbol) meth.sym);
				processed = true;
			}
			else
			{
				methodName = meth.name.toString();
			}
		}

		if (!processed && methodInvocation.args != null)
		{
			processed = extractParamTypesFromArgs(methodInvocation, paramsList);

			if (!processed && methodName != null)
			{
				// paramsList contains null values
				JavaClassInfo classInfo = context.getCurrent().getClassInfo();
				String fqClassName;
				if ("this".equals(methodName))
				{
					fqClassName = classInfo.getFqName();
					methodName = "<init>";
				}
				else if ("super".equals(methodName))
				{
					fqClassName = classInfo.getNameOfSuperClass();
					methodName = "<init>";
				}
				else
				{
					// TODO Think of something else. The methodParamNameService does only contain constructors.
					return null;
				}

				String[] fqParamClassNames = paramsList.toArray(String.class);
				String[] paramClassNames = methodParamNameService.getMethodParamClassNames(fqClassName, methodName, fqParamClassNames);

				if (paramClassNames != null)
				{
					paramsList.clear();
					paramsList.addAll(Arrays.asList(paramClassNames));

					processed = true;
				}
			}
		}

		if (!processed)
		{
			return null;
		}

		return paramsList;
	}

	protected boolean extractParamTypesFromArgs(final JCMethodInvocation methodInvocation, final IList<String> paramsList)
	{
		Boolean processed = astHelper.writeToStash(new IResultingBackgroundWorkerDelegate<Boolean>()
		{
			@Override
			public Boolean invoke() throws Throwable
			{
				IConversionContext context = DefaultMethodParameterProcessor.this.context.getCurrent();
				ILanguageHelper languageHelper = context.getLanguageHelper();
				com.sun.tools.javac.util.List<JCExpression> args = methodInvocation.args;
				boolean processed = true;
				int size = args.size();
				for (int a = 0; a < size; a++)
				{
					JCExpression arg = args.get(a);
					languageHelper.writeExpressionTree(arg);
					String typeOnStack = context.getTypeOnStack();

					if (typeOnStack == null)
					{
						paramsList.add(null);
						processed = false;
						continue;
					}

					JavaClassInfo paramClassInfo = classInfoManager.resolveClassInfo(typeOnStack, true);
					if (paramClassInfo == null)
					{
						throw new SnippetTrigger("No names for called methods parameters available").setContext(methodInvocation.toString());
					}
					String paramTypeName = paramClassInfo.getFqName();
					paramsList.add(paramTypeName);
				}
				return Boolean.valueOf(processed);
			}
		});
		return processed.booleanValue();
	}

	protected void getParamsList(IList<String> paramsList, MethodSymbol sym)
	{
		if (sym.params != null)
		{
			@SuppressWarnings("unchecked")
			List<VariableElement> retypedParams = (List<VariableElement>) (Object) sym.params;
			IList<String> paramTypeNames = languageHelper.createTypeNamesFromParams(retypedParams);
			paramsList.addAll(paramTypeNames);
		}
		else
		{
			throw new SnippetTrigger("No params found in MethodSymbol").setContext(sym.name.toString());
		}
	}
}
