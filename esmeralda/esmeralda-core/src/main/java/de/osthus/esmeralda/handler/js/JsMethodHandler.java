package de.osthus.esmeralda.handler.js;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.VarSymbol;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import demo.codeanalyzer.common.model.Method;

public class JsMethodHandler implements IJsMethodHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IJsHelper languageHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ISnippetManagerFactory snippetManagerFactory;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

	@Override
	public void handle()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void handle(HashMap<String, ArrayList<Method>> overloadedMethods)
	{
		IConversionContext context = this.context.getCurrent();
		final IWriter writer = context.getWriter();

		Method method = context.getMethod();

		IList<VariableElement> parameters = writeDocumentation(method, writer);

		String methodName = method.getName();

		languageHelper.newLineIndent();
		writer.append(methodName);
		if (overloadedMethods.containsKey(methodName))
		{
			// Add parameter type names to function name
			// TODO change names at method calls
			for (VariableElement param : parameters)
			{
				VarSymbol var = (VarSymbol) param;
				String paramTypeName = var.type.toString();
				paramTypeName = paramTypeName.replaceAll("\\.", "_");
				paramTypeName = StringConversionHelper.underscoreToCamelCase(objectCollector, paramTypeName);
				paramTypeName = StringConversionHelper.upperCaseFirst(objectCollector, paramTypeName);
				writer.append('_').append(paramTypeName);
			}
		}
		writer.append(": function(");
		boolean firstParam = true;
		for (VariableElement param : parameters)
		{
			firstParam = languageHelper.writeStringIfFalse(", ", firstParam);
			VarSymbol var = (VarSymbol) param;
			String paramName = var.name.toString();
			writer.append(paramName);
		}
		writer.append(") ");

		MethodTree methodTree = method.getMethodTree();

		if (methodTree.getModifiers().getFlags().contains(Modifier.ABSTRACT))
		{
			languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					languageHelper.newLineIndent();
					writer.append("// TODO: throw exception if not implemented");
				}
			});
			return;
		}

		ISnippetManager snippetManager = snippetManagerFactory.createSnippetManager(methodTree, languageHelper);
		context.setSnippetManager(snippetManager);
		try
		{
			BlockTree methodBodyBlock = methodTree.getBody();
			IStatementHandlerExtension<BlockTree> blockHandler = statementHandlerRegistry.getExtension(Lang.JS + methodBodyBlock.getKind());

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

	private IList<VariableElement> writeDocumentation(Method method, final IWriter writer)
	{
		languageHelper.startDocumentation();
		boolean hasContent = false;
		IList<VariableElement> parameters = method.getParameters();
		for (VariableElement param : parameters)
		{
			VarSymbol var = (VarSymbol) param;
			String type = var.type.toString();
			String convertedType = languageHelper.convertType(type, false);
			String name = var.name.toString();

			languageHelper.newLineIndentDocumentation();
			writer.append("@param {").append(convertedType).append("} ").append(name);
			hasContent = true;
		}
		String returnType = method.getReturnType();
		if (!"void".equals(returnType))
		{
			String convertedType = languageHelper.convertType(returnType, false);
			languageHelper.newLineIndentDocumentation();
			writer.append("@return {").append(convertedType).append("}");
			hasContent = true;
		}
		if (method.isPrivate())
		{
			languageHelper.newLineIndentDocumentation();
			writer.append("@private");
			hasContent = true;
		}
		if (!hasContent)
		{
			languageHelper.newLineIndentDocumentation();
		}
		languageHelper.endDocumentation();
		return parameters;
	}
}
