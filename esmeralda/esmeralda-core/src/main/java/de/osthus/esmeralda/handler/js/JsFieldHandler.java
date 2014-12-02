package de.osthus.esmeralda.handler.js;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Field;

public class JsFieldHandler implements IJsFieldHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsHelper languageHelper;

	@Override
	public void handle()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		Field field = context.getField();

		boolean privateStatic = field.isPrivate() && field.isStatic();

		languageHelper.newLineIntend();
		if (!privateStatic)
		{
			writer.append(field.getName()).append(": ");
			// TODO
			writer.append("undefined");
			writer.append(",");
		}
		else
		{
			writer.append("var ").append(field.getName());
			writer.append(";");
		}
	}
}
