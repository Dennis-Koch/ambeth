package de.osthus.esmeralda.handler.js;

import javax.lang.model.element.VariableElement;

import com.sun.tools.javac.code.Symbol.VarSymbol;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.misc.IWriter;
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

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		final IWriter writer = context.getWriter();

		Method method = context.getMethod();

		languageHelper.startDocumentation();
		IList<VariableElement> parameters = method.getParameters();
		for (VariableElement param : parameters)
		{
			VarSymbol var = (VarSymbol) param;
			String type = var.type.toString();
			String convertedType = languageHelper.convertType(type, false);
			String name = var.name.toString();

			languageHelper.newLineIndentDocumentation();
			writer.append("@param {").append(convertedType).append("} ").append(name);
		}
		String returnType = method.getReturnType();
		if (!"void".equals(returnType))
		{
			String convertedType = languageHelper.convertType(returnType, false);
			languageHelper.newLineIndentDocumentation();
			writer.append("@return {").append(convertedType).append("}");
		}
		if (method.isPrivate())
		{
			languageHelper.newLineIndentDocumentation();
			writer.append("@private");
		}
		languageHelper.endDocumentation();

		languageHelper.newLineIndent();
		writer.append(method.getName()).append(": function(");
		boolean firstParam = true;
		for (VariableElement param : parameters)
		{
			firstParam = languageHelper.writeStringIfFalse(", ", firstParam);
			VarSymbol var = (VarSymbol) param;
			String name = var.name.toString();
			writer.append(name);
		}
		writer.append(") ");

		languageHelper.scopeIntend(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				languageHelper.newLineIndent();
				writer.append("// ...");
			}
		});
	}
}
