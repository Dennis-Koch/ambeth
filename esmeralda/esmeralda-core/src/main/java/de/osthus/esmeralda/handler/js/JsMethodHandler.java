package de.osthus.esmeralda.handler.js;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.tools.javac.code.Symbol.VarSymbol;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodHandler;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import demo.codeanalyzer.common.model.Method;

public class JsMethodHandler implements IMethodHandler
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

	@Autowired(value = IJsOverloadManager.STATIC)
	protected IJsOverloadManager overloadManagerStatic;

	@Autowired(value = IJsOverloadManager.NON_STATIC)
	protected IJsOverloadManager overloadManagerNonStatic;

	@Autowired
	protected ISnippetManagerFactory snippetManagerFactory;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		final IWriter writer = context.getWriter();
		HashSet<String> methodScopeVars = languageHelper.getLanguageSpecific().getMethodScopeVars();

		Method method = context.getMethod();
		IJsOverloadManager overloadManager = method.isStatic() ? overloadManagerStatic : overloadManagerNonStatic;
		boolean hasOverloads = overloadManager.hasOverloads(method);
		IList<VariableElement> parameters = method.getParameters();
		String methodName = method.getName();

		writeDocumentation(method, writer);

		languageHelper.newLineIndent();
		writer.append('"');
		if (!method.isConstructor())
		{
			writer.append(methodName);
		}
		else
		{
			writer.append("constructor");
		}
		if (hasOverloads)
		{
			// Add parameter type names to function name
			String methodNamePostfix = languageHelper.createOverloadedMethodNamePostfix(languageHelper.createTypeNamesFromParams(parameters));
			writer.append(methodNamePostfix);
		}
		writer.append("\": function (");
		boolean firstParam = true;
		for (VariableElement param : parameters)
		{
			firstParam = languageHelper.writeStringIfFalse(", ", firstParam);
			VarSymbol var = (VarSymbol) param;
			String paramName = var.name.toString();
			methodScopeVars.add(paramName);
			languageHelper.writeVariableName(paramName);
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

		ISnippetManager originalSnippetManager = context.getSnippetManager();
		ISnippetManager snippetManager = snippetManagerFactory.createSnippetManager();
		context.setSnippetManager(snippetManager);
		try
		{
			BlockTree methodBodyBlock = methodTree.getBody();
			if (methodBodyBlock != null)
			{
				IStatementHandlerExtension<BlockTree> blockHandler = statementHandlerRegistry.getExtension(Lang.JS + methodBodyBlock.getKind());

				if (!method.isConstructor())
				{
					blockHandler.handle(methodBodyBlock);
				}
				else
				{
					// Skip only this() and super() calls without parameters
					boolean newSkip = isFirstStatementToSkip(methodBodyBlock);

					boolean oldSkip = context.isSkipFirstBlockStatement();
					context.setSkipFirstBlockStatement(newSkip);
					try
					{
						blockHandler.handle(methodBodyBlock);
					}
					finally
					{
						context.setSkipFirstBlockStatement(oldSkip);
					}
				}

				// Runs check for unused (old) snippet files for this method
				snippetManager.finished();
			}
			else
			{
				// FIXME Annotation methods have no body code
				writer.append("{}");
			}
		}
		finally
		{
			context.setSnippetManager(originalSnippetManager);
			languageHelper.getLanguageSpecific().getMethodScopeVars().clear();
		}

		if (!hasOverloads)
		{
			languageHelper.writeMetadata(method);
		}
	}

	protected void writeDocumentation(Method method, final IWriter writer)
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
			writer.append("@param {").append(convertedType).append("} ");
			languageHelper.writeVariableName(name);
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
	}

	protected boolean isFirstStatementToSkip(BlockTree methodBodyBlock)
	{
		List<? extends StatementTree> statements = methodBodyBlock.getStatements();
		if (statements.isEmpty())
		{
			return true;
		}

		StatementTree statementTree = statements.get(0);
		String firstStmtString = statementTree.toString();

		boolean skipFirst = "this()".equals(firstStmtString) || "super()".equals(firstStmtString);

		return skipFirst;
	}
}
