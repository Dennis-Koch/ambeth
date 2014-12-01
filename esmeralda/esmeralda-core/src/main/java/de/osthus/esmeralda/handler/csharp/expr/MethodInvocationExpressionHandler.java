package de.osthus.esmeralda.handler.csharp.expr;

import java.util.Arrays;
import java.util.regex.Matcher;

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

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.TypeResolveException;
import de.osthus.esmeralda.handler.ASTHelper;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;
import demo.codeanalyzer.common.model.MethodInfo;

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
		Method method = context.getMethod();

		if (methodInvocation.meth == null)
		{
			log.warn("Could not handle method invocation: " + methodInvocation);
			return;
		}
		String[] argTypes = languageHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String[], JCMethodInvocation>()
		{
			@Override
			public String[] invoke(JCMethodInvocation methodInvocation) throws Throwable
			{
				IConversionContext context = MethodInvocationExpressionHandler.this.context.getCurrent();

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
					typeOfOwner = languageHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String, JCExpression>()
					{
						@Override
						public String invoke(JCExpression state) throws Throwable
						{
							IConversionContext context = MethodInvocationExpressionHandler.this.context.getCurrent();
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
				typeOfOwner = languageHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String, JCExpression>()
				{
					@Override
					public String invoke(JCExpression state) throws Throwable
					{
						IConversionContext context = MethodInvocationExpressionHandler.this.context.getCurrent();
						languageHelper.writeExpressionTree(state);
						return context.getTypeOnStack();
					}
				}, meth.selected);
				owner = null;
				typeOfOwner = context.getTypeOnStack();
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

		final boolean fWriteOwnerAsType = writeOwnerAsType;
		final boolean fWriteMethodDot = writeMethodDot;
		final boolean fWriteOwnerAsTypeof = writeOwnerAsTypeof;

		final IOwnerWriter ownerWriter = new IOwnerWriter()
		{
			@Override
			public void writeOwner(String owner)
			{
				IConversionContext context = MethodInvocationExpressionHandler.this.context.getCurrent();
				IWriter writer = context.getWriter();

				if (owner != null)
				{
					if (fWriteOwnerAsType)
					{
						if (fWriteOwnerAsTypeof)
						{
							writer.append("typeof(");
						}
						languageHelper.writeType(owner);
						if (fWriteOwnerAsTypeof)
						{
							writer.append(')');
						}
					}
					else
					{
						writer.append(owner);
					}
				}
			}
		};

		transformedMethod.getParameterProcessor().processMethodParameters(methodInvocation, owner, transformedMethod, ownerWriter);
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
		if ("super".equals(methodName))
		{
			JavaClassInfo ownerInfo = context.resolveClassInfo(currOwner);
			return resolveMethodReturnType(ownerInfo.getNameOfSuperClass(), "this", argTypes);
		}
		if ("this".equals(methodName))
		{
			return Void.TYPE.getName();
		}
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
		Matcher paramGenericTypeMatcher = ASTHelper.genericTypePattern.matcher(typeName);
		if (paramGenericTypeMatcher.matches())
		{
			return paramGenericTypeMatcher.group(1);
		}
		return typeName;
	}
}
