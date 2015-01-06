package de.osthus.esmeralda.handler.js.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class JavaIoPrintstreamTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.io.PrintStream.class, "println", "console", "log", false, Boolean.TRUE, String.class);
		mapTransformation(java.io.PrintStream.class, "print", "console", "log", false, Boolean.TRUE, String.class);
	}
}
