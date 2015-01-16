package de.osthus.esmeralda.handler.js.transformer;

import java.util.List;

import javax.lang.model.element.VariableElement;

import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.js.IJsOverloadManager;
import de.osthus.esmeralda.misc.IWriter;

public class DefaultMethodParameterProcessor implements IMethodParameterProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IJsHelper languageHelper;

	@Autowired(value = IJsOverloadManager.STATIC)
	protected IJsOverloadManager overloadManagerStatic;

	@Autowired(value = IJsOverloadManager.NON_STATIC)
	protected IJsOverloadManager overloadManagerNonStatic;

	@Override
	public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod, IOwnerWriter ownerWriter)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		List<JCExpression> arguments = methodInvocation.getArguments();

		if (owner != null)
		{
			ownerWriter.writeOwner(owner);
			writer.append('.');
		}
		writer.append(transformedMethod.getName());

		if (!transformedMethod.isPropertyInvocation())
		{
			IJsOverloadManager overloadManager = transformedMethod.isStatic() ? overloadManagerStatic : overloadManagerNonStatic;
			if (overloadManager.hasOverloads(transformedMethod))
			{
				IList<VariableElement> paramsList = getParamsList(methodInvocation);
				String overloadedMethodNamePostfix = languageHelper.createOverloadedMethodNamePostfix(paramsList);
				writer.append(overloadedMethodNamePostfix);
			}
			writer.append('(');
			for (int a = 0, size = arguments.size(); a < size; a++)
			{
				JCExpression arg = arguments.get(a);
				if (a > 0)
				{
					writer.append(", ");
				}
				languageHelper.writeExpressionTree(arg);
			}
			writer.append(')');
		}
		else if (arguments.size() == 1)
		{
			writer.append(" = ");
			JCExpression argument = arguments.get(0);
			languageHelper.writeExpressionTree(argument);
			writer.append(';');
		}
		else if (arguments.size() > 0)
		{
			throw new IllegalStateException("Property assignment with multiple values not supported: " + methodInvocation);
		}
	}

	protected IList<VariableElement> getParamsList(JCMethodInvocation methodInvocation)
	{
		IList<VariableElement> paramsList = new ArrayList<VariableElement>();

		if (methodInvocation.meth == null)
		{
			return paramsList;
		}

		if (methodInvocation.meth instanceof JCIdent)
		{
			JCIdent meth = (JCIdent) methodInvocation.meth;
			if (meth.sym != null)
			{
				getParamsList(paramsList, (MethodSymbol) meth.sym);
			}
		}
		else if (methodInvocation.meth instanceof JCFieldAccess)
		{
			JCFieldAccess meth = (JCFieldAccess) methodInvocation.meth;
			if (meth.sym != null)
			{
				getParamsList(paramsList, (MethodSymbol) meth.sym);
			}
		}
		else
		{
			throw new RuntimeException("Have not yet thought of that one...");
		}

		return paramsList;
	}

	protected void getParamsList(IList<VariableElement> paramsList, MethodSymbol sym)
	{
		if (sym.params != null)
		{
			paramsList.addAll(sym.params);
		}
	}
}
