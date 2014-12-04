package de.osthus.esmeralda.handler.csharp;

import java.util.List;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IPostProcess;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IMethodTransformer;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class CsMethodHandler implements ICsMethodHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsHelper languageHelper;

	@Autowired
	protected IMethodTransformer methodTransformer;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ISnippetManagerFactory snippetManagerFactory;

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		Method method = context.getMethod();

		if (method.getOwningClass().isAnnotation())
		{
			handleMethodOfAnnotationType();
			return;
		}
		handleMethod();
	}

	protected void handleMethod()
	{
		IConversionContext context = this.context.getCurrent();
		Method method = context.getMethod();

		ITransformedMethod transformedMethod = methodTransformer.transformMethodDeclaration(method);

		if (transformedMethod == null)
		{
			return;
		}
		IWriter writer = context.getWriter();

		languageHelper.writeAnnotations(method);
		languageHelper.newLineIndent();

		boolean firstKeyWord = true;
		if (!method.getOwningClass().isInterface())
		{
			firstKeyWord = languageHelper.writeModifiers(method);
		}
		firstKeyWord = writeOverrideVirtual(method, transformedMethod, firstKeyWord);
		firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);

		if (!method.isConstructor())
		{
			languageHelper.writeType(method.getReturnType());
			writer.append(' ');
		}
		String methodName = StringConversionHelper.upperCaseFirst(objectCollector, method.getName());
		// TODO: remind of the changed method name on all invocations

		writer.append(methodName);

		// append generic type parameters
		MethodTree methodTree = method.getMethodTree();
		IList<VariableElement> parameters = method.getParameters();
		List<? extends TypeParameterTree> typeParameters = methodTree.getTypeParameters();
		if (typeParameters.size() > 0)
		{
			boolean firstGenericParameter = true;

			List<? extends VariableTree> methodTreeParameters = methodTree.getParameters();
			IList<Integer> parameterIndexToDeleteList = method.getParameterIndexToDelete();
			IdentityHashSet<TypeVar> fromArgumentsRequestedTypeVars = new IdentityHashSet<TypeVar>();

			for (Integer parameterIndexToDelete : parameterIndexToDeleteList)
			{
				int index = parameterIndexToDelete.intValue();

				VariableTree parameterToDelete = methodTreeParameters.get(index);
				JCVariableDecl variableDecl = (JCVariableDecl) parameterToDelete;
				ClassType type = (ClassType) variableDecl.type;
				if (type == null)
				{
					type = (ClassType) ((JCTypeApply) variableDecl.vartype).type;
				}
				for (Type typeParamsOfParameter : type.typarams_field)
				{
					fromArgumentsRequestedTypeVars.add((TypeVar) typeParamsOfParameter);
				}
			}
			for (VariableElement parameter : parameters)
			{
				VarSymbol varSymbol = (VarSymbol) parameter;
				if (!(varSymbol.type instanceof ClassType))
				{
					// a primitive type like boolean or int is just a "Type" not a "ClassType" but that is no problem because they can never be generic anyways
					continue;
				}
				for (Type typeParamsOfParameter : ((ClassType) varSymbol.type).typarams_field)
				{
					if (typeParamsOfParameter instanceof WildcardType)
					{
						continue;
					}
					fromArgumentsRequestedTypeVars.add((TypeVar) typeParamsOfParameter);
				}
			}
			for (TypeParameterTree typeParameter : typeParameters)
			{
				Type typeOfTypeParameter = ((JCTypeParameter) typeParameter).type;
				if (!fromArgumentsRequestedTypeVars.contains(typeOfTypeParameter))
				{
					// this type parameter has been erased
					continue;
				}
				if (firstGenericParameter)
				{
					writer.append('<');
				}
				firstGenericParameter = languageHelper.writeStringIfFalse(", ", firstGenericParameter);
				writer.append(typeParameter.toString());
			}
			if (!firstGenericParameter)
			{
				writer.append('>');
			}
		}
		writer.append('(');
		for (int a = 0, size = parameters.size(); a < size; a++)
		{
			VariableElement parameter = parameters.get(a);
			if (a > 0)
			{
				writer.append(", ");
			}
			languageHelper.writeType(parameter.asType().toString());
			writer.append(' ').append(parameter.getSimpleName());
		}
		writer.append(')');

		if (method.getOwningClass().isInterface() || method.isAbstract())
		{
			writer.append(';');
			return;
		}
		JCExpression superOrThisStatement = peekSuperOrThisStatement(method);
		if (superOrThisStatement != null)
		{
			writer.append(" : ");
			languageHelper.writeExpressionTree(superOrThisStatement);
		}
		ISnippetManager snippetManager = snippetManagerFactory.createSnippetManager(methodTree, languageHelper);
		context.setSnippetManager(snippetManager);
		try
		{
			BlockTree methodBodyBlock = methodTree.getBody();
			IStatementHandlerExtension<BlockTree> blockHandler = statementHandlerRegistry.getExtension(Lang.C_SHARP + methodBodyBlock.getKind());

			if (method.isConstructor())
			{
				boolean oldSkip = context.isSkipFirstBlockStatement();
				context.setSkipFirstBlockStatement(true);
				try
				{
					blockHandler.handle(methodBodyBlock);
				}
				finally
				{
					context.setSkipFirstBlockStatement(oldSkip);
				}
			}
			else
			{
				blockHandler.handle(methodBodyBlock);
			}

			// Starts check for unused (old) snippet files for this method
			snippetManager.finished();
		}
		finally
		{
			context.setSnippetManager(null);
		}
	}

	protected boolean writeOverrideVirtual(Method method, ITransformedMethod transformedMethod, boolean firstKeyWord)
	{
		if (method.isConstructor())
		{
			return firstKeyWord;
		}

		IConversionContext context = this.context;
		IWriter writer = context.getWriter();

		String currTypeName = context.getClassInfo().getNameOfSuperClass();
		boolean overrideNeeded = false;
		while (currTypeName != null)
		{
			JavaClassInfo currType = context.resolveClassInfo(currTypeName);
			if (currType == null)
			{
				break;
			}
			if (currType.hasMethodWithIdenticalSignature(transformedMethod))
			{
				overrideNeeded = true;
				break;
			}
			currTypeName = currType.getNameOfSuperClass();
		}

		if (overrideNeeded)
		{
			firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
			writer.append("override");
		}
		else if (!method.isFinal() && !method.isStatic() && !method.getOwningClass().isInterface() && !method.getOwningClass().isEnum()
				&& !method.getOwningClass().isAnnotation())
		{
			// a non-final method in java has to be marked as virtual in C#
			firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
			writer.append("virtual");
		}
		return firstKeyWord;
	}

	protected JCExpression peekSuperOrThisStatement(Method method)
	{
		MethodTree methodTree = method.getMethodTree();
		if (!methodTree.getName().contentEquals("<init>"))
		{
			return null;
		}
		List<? extends StatementTree> statements = methodTree.getBody().getStatements();
		if (statements.size() == 0)
		{
			return null;
		}
		StatementTree firstStatement = statements.get(0);
		if (!(firstStatement instanceof JCExpressionStatement))
		{
			return null;
		}
		JCExpression expression = ((JCExpressionStatement) firstStatement).getExpression();
		if (!(expression instanceof JCMethodInvocation))
		{
			return null;
		}
		String methodName = ((JCMethodInvocation) expression).getMethodSelect().toString();
		if ("super".equals(methodName) || "this".equals(methodName))
		{
			return expression;
		}
		return null;
	}

	protected void handleMethodOfAnnotationType()
	{
		IConversionContext context = this.context.getCurrent();
		Method method = context.getMethod();
		IWriter writer = context.getWriter();

		MethodTree methodTree = method.getMethodTree();
		final Tree defaultValue = methodTree.getDefaultValue();
		// handle special case of annotation property
		final String propertyName = StringConversionHelper.upperCaseFirst(objectCollector, method.getName());
		// TODO: remind of the changed method name on all invocations

		languageHelper.writeAnnotations(method);
		languageHelper.newLineIndent();

		writer.append("public ");
		languageHelper.writeType(method.getReturnType());
		writer.append(' ');
		writer.append(propertyName);
		writer.append(" { get; set; }");

		if (defaultValue != null)
		{
			context.queuePostProcess(new IPostProcess()
			{
				@Override
				public void postProcess()
				{
					IConversionContext context = CsMethodHandler.this.context.getCurrent();
					IWriter writer = context.getWriter();
					writer.append(propertyName).append(" = ");
					if (defaultValue instanceof ExpressionTree)
					{
						languageHelper.writeExpressionTree(defaultValue);
					}
					else
					{
						writer.append(defaultValue.toString());
					}
				}
			});
		}
	}
}
