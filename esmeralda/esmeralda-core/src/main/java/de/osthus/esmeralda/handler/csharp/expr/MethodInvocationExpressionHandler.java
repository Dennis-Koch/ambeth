package de.osthus.esmeralda.handler.csharp.expr;

import java.util.Arrays;
import java.util.regex.Matcher;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.TypeResolveException;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class MethodInvocationExpressionHandler extends AbstractExpressionHandler<JCMethodInvocation>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IMethodTransformer methodTransformer;

	@Override
	protected void handleExpressionIntern(JCMethodInvocation methodInvocation)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		if (methodInvocation.meth == null)
		{
			log.warn("Could not handle method invocation: " + methodInvocation);
			return;
		}
		String methodName;
		String owner;
		boolean writeOwnerAsType = false;
		boolean writeMethodDot = false;

		String typeOfOwner;
		if (methodInvocation.meth instanceof JCIdent)
		{
			JCIdent ident = (JCIdent) methodInvocation.meth;
			methodName = ident.name.toString();
			owner = null;
			typeOfOwner = context.getClassInfo().getFqName();
		}
		else
		{
			JCFieldAccess meth = (JCFieldAccess) methodInvocation.meth;
			if (meth.selected instanceof JCLiteral)
			{
				owner = ((JCLiteral) meth.selected).value.toString();
				typeOfOwner = context.resolveClassInfo(((JCLiteral) meth.selected).type.toString()).getFqName();
			}
			else if (meth.selected instanceof JCFieldAccess)
			{
				JCFieldAccess fieldAccess = (JCFieldAccess) meth.selected;
				JavaClassInfo classInfoFromFA = context.resolveClassInfo(fieldAccess.toString(), true);
				if (classInfoFromFA != null)
				{
					typeOfOwner = classInfoFromFA.getFqName();
					writeOwnerAsType = true;
				}
				else
				{
					languageHelper.writeExpressionTree(fieldAccess);
					typeOfOwner = context.getTypeOnStack();
				}
				owner = null;
				writeMethodDot = true;
			}
			else if (meth.selected instanceof JCMethodInvocation || meth.selected instanceof JCNewClass || meth.selected instanceof JCParens
					|| meth.selected instanceof JCArrayAccess)
			{
				languageHelper.writeExpressionTree(meth.selected);
				owner = null;
				typeOfOwner = context.getTypeOnStack();
				writeMethodDot = true;
			}
			else
			{
				JCIdent selected = (JCIdent) meth.selected;
				if (selected.sym instanceof VarSymbol)
				{
					owner = selected.sym.toString();
					typeOfOwner = selected.sym.type != null ? context.resolveClassInfo(selected.sym.type.toString()).getFqName() : languageHelper
							.resolveTypeFromVariableName(owner);
				}
				else if (selected.sym instanceof ClassSymbol)
				{
					owner = selected.type.toString();
					typeOfOwner = context.resolveClassInfo(selected.type.toString()).getFqName();
					writeOwnerAsType = true;
				}
				else if (selected.sym == null)
				{
					// resolve owner by scanning the method signature & method body
					owner = selected.toString();
					typeOfOwner = languageHelper.resolveTypeFromVariableName(owner);
				}
				else
				{
					throw new IllegalStateException("Unknown symbol type: " + selected.sym + " (" + selected.sym.getClass().getName() + ")");
				}
			}
			methodName = meth.name.toString();
		}
		ITransformedMethod transformedMethod = methodTransformer.transform(typeOfOwner, methodName, methodInvocation.getArguments());
		if (writeOwnerAsType)
		{
			languageHelper.writeType(transformedMethod.getOwner());
		}
		else if (owner != null)
		{
			writer.append(owner);
		}
		if (writeMethodDot || owner != null)
		{
			writer.append('.');
		}
		writer.append(transformedMethod.getName());

		String[] argTypes = null;
		if (!transformedMethod.isPropertyInvocation())
		{
			writer.append('(');
			List<JCExpression> arguments = methodInvocation.getArguments();
			argTypes = new String[arguments.size()];
			for (int a = 0, size = arguments.size(); a < size; a++)
			{
				JCExpression arg = arguments.get(a);
				if (a > 0)
				{
					writer.append(", ");
				}
				languageHelper.writeExpressionTree(arg);
				String typeOnStack = context.getTypeOnStack();
				argTypes[a] = extractNonGenericType(typeOnStack);
			}
			writer.append(')');
		}
		else if (methodInvocation.getArguments().size() > 0)
		{
			// C# will be an assignment to a property (setter-semantics)
			writer.append(" = ");
			boolean firstArgument = true;
			for (JCExpression argument : methodInvocation.getArguments())
			{
				firstArgument = languageHelper.writeStringIfFalse(", ", firstArgument);
				languageHelper.writeExpressionTree(argument);
			}
		}
		if (methodInvocation.type != null)
		{
			context.setTypeOnStack(methodInvocation.type.toString());
			return;
		}
		String returnType = resolveMethodReturnType(typeOfOwner, methodName, argTypes);
		context.setTypeOnStack(returnType);
	}

	protected String resolveMethodReturnType(String currOwner, String methodName, String... argTypes)
	{
		while (currOwner != null)
		{
			JavaClassInfo ownerInfo = context.resolveClassInfo(currOwner);
			if (ownerInfo == null)
			{
				throw new IllegalStateException("ClassInfo not resolved: '" + currOwner + "'");
			}
			for (Method method : ownerInfo.getMethods())
			{
				if (!method.getName().equals(methodName))
				{
					continue;
				}
				IList<VariableElement> parameters = method.getParameters();
				if (parameters.size() != argTypes.length)
				{
					continue;
				}
				boolean identicalParameterTypes = true;
				for (int a = argTypes.length; a-- > 0;)
				{
					String argType = argTypes[a];
					TypeMirror parameterType = parameters.get(a).asType();
					String parameterTypeName;
					if (parameterType instanceof TypeVar)
					{
						parameterTypeName = ((TypeVar) parameterType).getUpperBound().toString();
					}
					else
					{
						parameterTypeName = parameterType.toString();
					}
					parameterTypeName = extractNonGenericType(parameterTypeName);
					boolean parameterMatch = false;
					while (argType != null)
					{
						if (parameterTypeName.equals(argType))
						{
							parameterMatch = true;
							break;
						}
						JavaClassInfo argClassInfo = context.resolveClassInfo(argType);
						if (argClassInfo == null)
						{
							break;
						}
						argType = argClassInfo.getNameOfSuperClass();
					}
					if (!parameterMatch)
					{
						identicalParameterTypes = false;
						break;
					}
				}
				if (!identicalParameterTypes)
				{
					continue;
				}
				return method.getReturnType();
			}
			currOwner = ownerInfo.getNameOfSuperClass();
		}
		throw new TypeResolveException("No matching method found '" + methodName + "(" + Arrays.toString(argTypes) + ")");
	}

	protected String extractNonGenericType(String typeName)
	{
		Matcher paramGenericTypeMatcher = ConversionContext.genericTypePattern.matcher(typeName);
		if (paramGenericTypeMatcher.matches())
		{
			return paramGenericTypeMatcher.group(1);
		}
		return typeName;
	}
}
