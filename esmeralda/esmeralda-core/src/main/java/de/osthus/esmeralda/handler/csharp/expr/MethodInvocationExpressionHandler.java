package de.osthus.esmeralda.handler.csharp.expr;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.locks.Condition;
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
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.TypeResolveException;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.ClassFile;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class MethodInvocationExpressionHandler extends AbstractExpressionHandler<JCMethodInvocation>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
		String typeOfOwner;
		if (methodInvocation.meth instanceof JCIdent)
		{
			JCIdent ident = (JCIdent) methodInvocation.meth;
			methodName = ident.name.toString();
			owner = null;
			typeOfOwner = context.getClassInfo().getPackageName() + "." + context.getClassInfo().getName();
		}
		else
		{
			JCFieldAccess meth = (JCFieldAccess) methodInvocation.meth;
			if (meth.selected instanceof JCLiteral)
			{
				owner = ((JCLiteral) meth.selected).value.toString();
				typeOfOwner = ((JCLiteral) meth.selected).type.toString();
			}
			else if (meth.selected instanceof JCFieldAccess)
			{
				JCFieldAccess fieldAccess = (JCFieldAccess) meth.selected;
				languageHelper.writeExpressionTree(fieldAccess);
				owner = null;
				if (fieldAccess.type == null)
				{// TODO: handle this case. Is this an error in the sources? Is there something missing?
					throw new TypeResolveException("No type in method invocation '" + methodInvocation + "'");
				}
				typeOfOwner = fieldAccess.type.toString();
			}
			else if (meth.selected instanceof JCMethodInvocation || meth.selected instanceof JCNewClass || meth.selected instanceof JCParens
					|| meth.selected instanceof JCArrayAccess)
			{
				languageHelper.writeExpressionTree(meth.selected);
				owner = null;
				typeOfOwner = context.getTypeOnStack();
			}
			else
			{
				JCIdent selected = (JCIdent) meth.selected;
				if (selected.sym instanceof VarSymbol)
				{
					owner = selected.sym.toString();
					typeOfOwner = selected.sym.type != null ? selected.sym.type.toString() : resolveTypeFromVariableName(owner);
				}
				else if (selected.sym instanceof ClassSymbol)
				{
					owner = selected.type.toString();
					typeOfOwner = selected.type.toString();
					writeOwnerAsType = true;
				}
				else if (selected.sym == null)
				{
					// resolve owner by scanning the method signature & method body
					owner = selected.toString();
					typeOfOwner = resolveTypeFromVariableName(owner);
				}
				else
				{
					throw new IllegalStateException("Unknown symbol type: " + selected.sym + " (" + selected.sym.getClass().getName() + ")");
				}
			}
			methodName = meth.name.toString();
		}
		String nonGenericTypeOfOwner = typeOfOwner;
		Matcher genericTypeMatcher = ConversionContext.genericTypePattern.matcher(nonGenericTypeOfOwner);
		if (genericTypeMatcher.matches())
		{
			nonGenericTypeOfOwner = genericTypeMatcher.group(1);
		}
		if (EnumSet.class.getName().equals(nonGenericTypeOfOwner))
		{
			// if we handle the enums either as C# enums or as static readonly objects will be decided by the flags-annotation
			// TODO: read integrity-xml of .NET and look whether the enum has this annotation
			log.warn("Could not handle method invocation: " + methodInvocation);
			return;
		}
		if (Condition.class.getName().equals(nonGenericTypeOfOwner))
		{
			// TODO: handle java.concurrent.lock API
			log.warn("Could not handle method invocation: " + methodInvocation);
			return;
		}
		if (owner != null)
		{
			if (writeOwnerAsType)
			{
				languageHelper.writeType(owner);
			}
			else
			{
				writer.append(owner);
			}
		}
		writer.append('.');

		String formattedMethodName = StringConversionHelper.upperCaseFirst(objectCollector, methodName);
		boolean isPropertyInvocation = false;
		if (Class.class.getName().equals(nonGenericTypeOfOwner))
		{
			if ("getSimpleName".equals(methodName))
			{
				formattedMethodName = "Name";
				isPropertyInvocation = true;
			}
			else if ("getName".equals(methodName))
			{
				formattedMethodName = "FullName";
				isPropertyInvocation = true;
			}
		}
		writer.append(formattedMethodName);

		String[] argTypes = null;
		if (!isPropertyInvocation)
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
				System.out.println();
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

	protected String resolveTypeFromVariableName(String variableName)
	{
		ParamChecker.assertParamNotNullOrEmpty(variableName, "variableName");
		Method method = context.getMethod();
		// look for stack variables first
		for (VariableElement parameter : method.getParameters())
		{
			if (variableName.equals(parameter.getSimpleName().toString()))
			{
				return parameter.asType().toString();
			}
		}
		// look for declared fields up the whole class hierarchy
		ClassFile classInfo = method.getOwningClass();
		while (classInfo != null)
		{
			for (Field field : classInfo.getFields())
			{
				if (variableName.equals(field.getName()))
				{
					return field.getFieldType().toString();
				}
			}
			String nameOfSuperClass = classInfo.getNameOfSuperClass();
			if (nameOfSuperClass == null)
			{
				break;
			}
			classInfo = context.resolveClassInfo(nameOfSuperClass);
		}
		throw new IllegalStateException("Could not resolve variable '" + variableName + "' in method signature: " + method);
	}
}
