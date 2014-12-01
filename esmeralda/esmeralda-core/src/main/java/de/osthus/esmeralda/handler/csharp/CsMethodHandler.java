package de.osthus.esmeralda.handler.csharp;

import java.util.List;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Type.WildcardType;
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
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class CsMethodHandler implements INodeHandlerExtension
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
		IWriter writer = context.getWriter();

		languageHelper.writeAnnotations(method);
		languageHelper.newLineIntend();

		boolean firstKeyWord = true;
		if (!method.getOwningClass().isInterface())
		{
			firstKeyWord = languageHelper.writeModifiers(method);
		}

		String currTypeName = context.getClassInfo().getNameOfSuperClass();
		boolean overrideNeeded = false;
		while (currTypeName != null)
		{
			JavaClassInfo currType = context.resolveClassInfo(currTypeName);
			if (currType == null)
			{
				break;
			}
			if (currType.hasMethodWithIdenticalSignature(method))
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
		else if (!method.isFinal() && !method.getOwningClass().isInterface() && !method.getOwningClass().isEnum() && !method.getOwningClass().isAnnotation())
		{
			// a non-final method in java has to be marked as virtual in C#
			firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
			writer.append("virtual");
		}
		firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
		languageHelper.writeType(method.getReturnType());

		writer.append(' ');
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

		ISnippetManager snippetManager = snippetManagerFactory.createSnippetManager(methodTree, languageHelper);
		context.setSnippetManager(snippetManager);
		try
		{
			IStatementHandlerExtension<BlockTree> blockHandler = statementHandlerRegistry.get(Lang.C_SHARP + Kind.BLOCK);
			BlockTree methodBodyBlock = methodTree.getBody();
			blockHandler.handle(methodBodyBlock);

			// Starts check for unused (old) snippet files for this method
			snippetManager.finished();
		}
		finally
		{
			context.setSnippetManager(null);
		}
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
		languageHelper.newLineIntend();

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
