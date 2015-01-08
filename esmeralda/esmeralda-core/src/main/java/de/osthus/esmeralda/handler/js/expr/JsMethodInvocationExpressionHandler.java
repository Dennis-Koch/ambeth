package de.osthus.esmeralda.handler.js.expr;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.ParamHolder;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.TypeResolveException;
import de.osthus.esmeralda.handler.ASTHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.js.IJsMethodTransformer;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;
import demo.codeanalyzer.common.model.MethodInfo;

public class JsMethodInvocationExpressionHandler extends AbstractExpressionHandler<JCMethodInvocation>
{
	public static final Pattern trimCaptureOfPattern = Pattern.compile("capture#\\d+ of ");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IJsMethodTransformer methodTransformer;

	@Override
	protected void handleExpressionIntern(JCMethodInvocation methodInvocation)
	{
		IConversionContext context = this.context.getCurrent();
		final ILanguageHelper languageHelper = context.getLanguageHelper();

		Method method = context.getMethod();
		IWriter writer = context.getWriter();

		if (methodInvocation.meth == null)
		{
			log.warn("Could not handle method invocation: " + methodInvocation);
			return;
		}
		String[] argTypes = astHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String[], JCMethodInvocation>()
		{
			@Override
			public String[] invoke(JCMethodInvocation methodInvocation) throws Throwable
			{
				IConversionContext context = JsMethodInvocationExpressionHandler.this.context.getCurrent();

				String oldTypeOnStack = context.getTypeOnStack();

				List<JCExpression> arguments = methodInvocation.getArguments();
				String[] argTypes = new String[arguments.size()];
				for (int a = 0, size = arguments.size(); a < size; a++)
				{
					JCExpression arg = arguments.get(a);
					languageHelper.writeExpressionTree(arg);
					argTypes[a] = context.getTypeOnStack();
					context.setTypeOnStack(oldTypeOnStack);
				}
				return argTypes;
			}
		}, methodInvocation);

		String methodName;
		String owner;
		boolean writeOwnerAsType = false;
		boolean writeMethodDot = false;
		boolean writeOwnerAsTypeof = false;

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
					typeOfOwner = astHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String, JCExpression>()
					{
						@Override
						public String invoke(JCExpression state) throws Throwable
						{
							IConversionContext context = JsMethodInvocationExpressionHandler.this.context.getCurrent();
							languageHelper.writeExpressionTree(state);
							return context.getTypeOnStack();
						}
					}, fieldAccess);
				}
				owner = null;
				writeMethodDot = true;
			}
			else if (meth.selected instanceof JCMethodInvocation || meth.selected instanceof JCNewClass || meth.selected instanceof JCParens
					|| meth.selected instanceof JCArrayAccess)
			{
				typeOfOwner = astHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String, JCExpression>()
				{
					@Override
					public String invoke(JCExpression state) throws Throwable
					{
						IConversionContext context = JsMethodInvocationExpressionHandler.this.context.getCurrent();
						languageHelper.writeExpressionTree(state);
						return context.getTypeOnStack();
					}
				}, meth.selected);
				if (meth.selected instanceof JCNewClass)
				{
					languageHelper.writeExpressionTree(meth.selected);
					writer.append('.');
				}
				owner = null;
				writeMethodDot = true;
			}
			else
			{
				JCIdent selected = (JCIdent) meth.selected;
				Symbol sym = selected.sym;
				if (sym instanceof VarSymbol)
				{
					owner = sym.toString();

					if (method != null)
					{
						IList<Integer> parameterIndexToDelete = ((MethodInfo) method).getParameterIndexToDelete();
						if (parameterIndexToDelete.size() > 0)
						{
							java.util.List<? extends VariableTree> parameters = method.getMethodTree().getParameters();
							for (Integer parameterIndexToErase : parameterIndexToDelete)
							{
								VariableTree variableElement = parameters.get(parameterIndexToErase.intValue());

								if (sym == ((JCVariableDecl) variableElement).sym) // reference equals intended
								{
									Type typeVar = ((ClassType) ((VarSymbol) sym).type).typarams_field.get(0);
									owner = typeVar.toString();
									writeOwnerAsType = true;
									writeOwnerAsTypeof = true;
									break;
								}
							}
						}
					}
					typeOfOwner = selected.sym.type != null ? context.resolveClassInfo(selected.sym.type.toString()).getFqName() : astHelper
							.resolveTypeFromVariableName(owner);
				}
				else if (sym instanceof ClassSymbol)
				{
					owner = selected.type.toString();
					typeOfOwner = context.resolveClassInfo(selected.type.toString()).getFqName();
					writeOwnerAsType = true;
				}
				else if (sym == null)
				{
					// resolve owner by scanning the method signature & method body
					owner = selected.toString();
					typeOfOwner = astHelper.resolveTypeFromVariableName(owner);
				}
				else
				{
					throw new IllegalStateException("Unknown symbol type: " + selected.sym + " (" + selected.sym.getClass().getName() + ")");
				}
			}
			methodName = meth.name.toString();
		}
		ITransformedMethod transformedMethod = methodTransformer.transform(typeOfOwner, methodName, methodInvocation.getArguments());

		context.addCalledMethod(transformedMethod);

		final boolean fWriteOwnerAsType = writeOwnerAsType;
		final boolean fWriteMethodDot = writeMethodDot;

		if (Boolean.TRUE.equals(transformedMethod.isWriteOwner()) || (transformedMethod.isWriteOwner() == null && fWriteOwnerAsType))
		{
			owner = transformedMethod.getOwner();
		}
		writeOwnerAsType |= transformedMethod.isOwnerAType();

		final IOwnerWriter ownerWriter = new IOwnerWriter()
		{
			@Override
			public void writeOwner(String owner)
			{
				IConversionContext context = JsMethodInvocationExpressionHandler.this.context.getCurrent();
				IWriter writer = context.getWriter();

				if (owner != null)
				{
					if (fWriteOwnerAsType)
					{
						languageHelper.writeType(owner);
					}
					else
					{
						owner = context.getTransformedSymbol(owner);
						writer.append(owner);
					}
				}
			}
		};

		transformedMethod.getParameterProcessor().processMethodParameters(methodInvocation, owner, transformedMethod, ownerWriter);
		if (methodInvocation.type != null)
		{
			String returnType = methodInvocation.type.toString();
			returnType = trimCaptureOfPattern.matcher(returnType).replaceAll("");
			context.setTypeOnStack(returnType);
			return;
		}
		String returnType = resolveMethodReturnType(typeOfOwner, methodName, argTypes);
		context.setTypeOnStack(returnType);
	}

	protected String resolveMethodReturnType(String currOwner, String methodName, String... argTypes)
	{
		if ("super".equals(methodName))
		{
			JavaClassInfo ownerInfo = context.resolveClassInfo(currOwner);
			return resolveMethodReturnType(ownerInfo.getNameOfSuperClass(), "this", argTypes);
		}
		if ("this".equals(methodName))
		{
			return Void.TYPE.getName();
		}
		ParamHolder<Method> methodWithBoxingPH = new ParamHolder<Method>();
		Method method = searchMethodOnType(currOwner, methodName, argTypes, new HashSet<String>(), methodWithBoxingPH);
		if (method != null)
		{
			return method.getReturnType();
		}
		if (methodWithBoxingPH.getValue() != null)
		{
			// if no method with object inheritance match could be found we return the auto-box match if any
			return methodWithBoxingPH.getValue().getReturnType();
		}
		throw new TypeResolveException("No matching method found '" + methodName + "(" + Arrays.toString(argTypes) + ")");
	}

	protected Method searchMethodOnType(String type, String methodName, String[] argTypes, Set<String> alreadyTriedTypes, ParamHolder<Method> methodWithBoxingPH)
	{
		if (!alreadyTriedTypes.add(type))
		{
			// already tried
			return null;
		}
		Method methodWithBoxing = null;
		Method methodOnInterface = null;
		String currType = type;
		while (currType != null)
		{
			JavaClassInfo ownerInfo = context.resolveClassInfo(currType);
			if (ownerInfo == null)
			{
				throw new IllegalStateException("ClassInfo not resolved: '" + currType + "'");
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
				if (matchParametersByInheritance(argTypes, parameters))
				{
					return method;
				}
				if (methodWithBoxing != null)
				{
					// we already found our auto-box candidate
					continue;
				}
				// we check if the current method might match for auto-boxing
				if (matchParametersByBoxing(argTypes, parameters))
				{
					methodWithBoxing = method;
				}
			}
			if (methodOnInterface == null)
			{
				for (String nameOfInterface : ownerInfo.getNameOfInterfaces())
				{
					methodOnInterface = searchMethodOnType(nameOfInterface, methodName, argTypes, alreadyTriedTypes, methodWithBoxingPH);
					if (methodOnInterface != null)
					{
						break;
					}
				}
			}
			currType = ownerInfo.getNameOfSuperClass();
		}
		if (methodOnInterface != null)
		{
			return methodOnInterface;
		}
		return methodWithBoxing;
	}

	protected boolean matchParametersByBoxing(String[] argTypes, IList<VariableElement> parameters)
	{
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
			if (matchParameterByInheritance(argType, parameterTypeName))
			{
				continue;
			}
			if (!matchParameterByBoxing(argType, parameterTypeName))
			{
				return false;
			}
		}
		return true;
	}

	protected boolean matchParametersByInheritance(String[] argTypes, IList<VariableElement> parameters)
	{
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
			if (!matchParameterByInheritance(argType, parameterTypeName))
			{
				return false;
			}
		}
		return true;
	}

	protected boolean matchParameterByBoxing(String argType, String parameterTypeName)
	{
		String boxedArgType = ASTHelper.unboxedToBoxedTypeMap.get(argType);
		if (boxedArgType != null)
		{
			return matchParameterByInheritance(boxedArgType, parameterTypeName);
		}
		String unboxedArgType = ASTHelper.boxedToUnboxedTypeMap.get(argType);
		// check for auto-unboxing
		return (unboxedArgType != null && parameterTypeName.equals(unboxedArgType));
	}

	protected boolean matchParameterByInheritance(String argType, String parameterTypeName)
	{
		String nonGenericParameterTypeName = astHelper.extractNonGenericType(parameterTypeName);
		while (argType != null)
		{
			if (parameterTypeName.equals(argType) || nonGenericParameterTypeName.equals(argType))
			{
				return true;
			}
			JavaClassInfo argClassInfo = context.resolveClassInfo(argType);
			if (argClassInfo == null)
			{
				return false;
			}
			for (String nameOfInterface : argClassInfo.getNameOfInterfaces())
			{
				if (matchParameterByInheritance(nameOfInterface, parameterTypeName))
				{
					return true;
				}
			}
			argType = argClassInfo.getNameOfSuperClass();
		}
		return false;
	}
}
