package de.osthus.esmeralda.handler.csharp;

import javax.lang.model.element.VariableElement;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class CsharpMethodNodeHandler implements INodeHandlerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ISnippetManagerFactory snippetManagerFactory;

	@Override
	public void handle(Object astNode)
	{
		IConversionContext context = this.context.getCurrent();
		Method method = context.getMethod();
		IWriter writer = context.getWriter();

		languageHelper.newLineIntend();
		languageHelper.writeAnnotations(method);
		languageHelper.newLineIntend();

		boolean firstKeyWord = languageHelper.writeModifiers(method);
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

		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				// TODO does not work yet
				// ISnippetManager snippetManager = snippetManagerFactory.createSnippetManager(astNode, context, languageHelper);

				// method body

				// snippetManager.finished();
			}
		});
	}
}
