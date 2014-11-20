package de.osthus.esmeralda.handler.csharp;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IPostProcess;
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
	public void handle(Tree astNode)
	{
		IConversionContext context = this.context.getCurrent();
		Method method = context.getMethod();
		IWriter writer = context.getWriter();

		languageHelper.writeAnnotations(method);
		languageHelper.newLineIntend();
		if (method.getOwningClass().isAnnotation())
		{
			MethodTree methodTree = method.getMethodTree();
			final Tree defaultValue = methodTree.getDefaultValue();
			// handle special case of annotation property
			final String propertyName = StringConversionHelper.upperCaseFirst(objectCollector, method.getName());
			// TODO: remind of the changed method name on all invocations

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
							languageHelper.writeExpressionTree((ExpressionTree) defaultValue);
						}
						else
						{
							writer.append(defaultValue.toString());
						}
					}
				});
			}
			return;
		}
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
		firstKeyWord = languageHelper.writeStringIfFalse(" ", firstKeyWord);
		languageHelper.writeType(method.getReturnType());

		writer.append(' ');
		String methodName = StringConversionHelper.upperCaseFirst(objectCollector, method.getName());
		// TODO: remind of the changed method name on all invocations

		writer.append(methodName).append('(');
		IList<VariableElement> parameters = method.getParameters();
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
		MethodTree methodTree = (MethodTree) astNode;
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
}
