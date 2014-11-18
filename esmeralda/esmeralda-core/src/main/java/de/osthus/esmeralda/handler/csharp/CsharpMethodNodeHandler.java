package de.osthus.esmeralda.handler.csharp;

import java.io.Writer;

import javax.lang.model.element.VariableElement;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.handler.INodeHandlerExtension;
import demo.codeanalyzer.common.model.Method;

public class CsharpMethodNodeHandler implements INodeHandlerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICsharpHelper languageHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void handle(Object astNode, ConversionContext context, Writer writer) throws Throwable
	{
		Method method = context.getMethod();
		languageHelper.newLineIntend(context, writer);
		languageHelper.writeAnnotations(method, context, writer);
		languageHelper.newLineIntend(context, writer);

		boolean firstKeyWord = languageHelper.writeModifiers(method, context, writer);
		firstKeyWord = languageHelper.spaceIfFalse(firstKeyWord, context, writer);
		languageHelper.writeType(method.getReturnType(), context, writer).append(' ');
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
			languageHelper.writeType(parameter.asType().toString(), context, writer).append(' ');
			writer.append(parameter.getSimpleName());
		}
		writer.append(')');

		languageHelper.scopeIntend(context, writer, new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				// method body
			}
		});
	}
}
