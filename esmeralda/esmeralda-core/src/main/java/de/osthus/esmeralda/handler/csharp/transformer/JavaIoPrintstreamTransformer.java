package de.osthus.esmeralda.handler.csharp.transformer;

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

		mapTransformationOverloads(java.io.PrintStream.class, "println", "System.Console", "WriteLine", Boolean.TRUE, true, boolean.class, Boolean.class,
				boolean[].class, double.class, Double.class, float.class, Float.class, Object.class, String.class, long.class, Long.class, int.class,
				Integer.class);
		mapTransformationOverloads(java.io.PrintStream.class, "print", "System.Console", "Write", Boolean.TRUE, true, boolean.class, Boolean.class,
				boolean[].class, double.class, Double.class, float.class, Float.class, Object.class, String.class, long.class, Long.class, int.class,
				Integer.class);
	}
}
