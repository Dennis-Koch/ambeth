package de.osthus.esmeralda.handler.uni.expr;

import java.util.regex.Pattern;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
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
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.AbstractExpressionHandler;
import de.osthus.esmeralda.handler.IExpressionHandler;
import de.osthus.esmeralda.handler.IExpressionHandlerRegistry;
import de.osthus.esmeralda.handler.IMethodMatcher;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;
import demo.codeanalyzer.common.model.MethodInfo;

public class MethodInvocationExpressionHandler extends AbstractExpressionHandler<JCMethodInvocation>
{
	public static final Pattern trimCaptureOfPattern = Pattern.compile("capture#\\d+ of ");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IExpressionHandlerRegistry expressionHandlerRegistry;

	@Autowired
	protected IMethodMatcher methodMatcher;

	@Autowired
	protected IMethodTransformer methodTransformer;

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
				final JCLiteral literal = (JCLiteral) meth.selected;
				String lang = context.getLanguage();
				Kind kind = meth.selected.getKind(); // There are multiple literal kinds
				final IExpressionHandler expressionHandler = expressionHandlerRegistry.getExtension(lang + kind);
				owner = astHelper.writeToStash(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						expressionHandler.handleExpression(literal);
					}
				});
				typeOfOwner = context.resolveClassInfo(literal.type.toString()).getFqName();
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
				typeOfOwner = astHelper.writeToStash(new IResultingBackgroundWorkerParamDelegate<String, JCExpression>()
				{
					@Override
					public String invoke(JCExpression state) throws Throwable
					{
						IConversionContext context = MethodInvocationExpressionHandler.this.context.getCurrent();
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

		if (Boolean.TRUE.equals(transformedMethod.isWriteOwner()) || (transformedMethod.isWriteOwner() == null && writeOwnerAsType))
		{
			owner = transformedMethod.getOwner();
		}
		writeOwnerAsType |= transformedMethod.isOwnerAType();

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
							languageHelper.writeAsTypeOf(owner);
						}
						else
						{
							languageHelper.writeType(owner);
						}
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
		String returnType = methodMatcher.resolveMethodReturnType(typeOfOwner, methodName, argTypes);
		context.setTypeOnStack(returnType);
	}
}
