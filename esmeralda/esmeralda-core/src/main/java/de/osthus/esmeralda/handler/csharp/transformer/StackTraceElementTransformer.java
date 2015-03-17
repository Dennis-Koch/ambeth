package de.osthus.esmeralda.handler.csharp.transformer;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;

public class StackTraceElementTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.lang.StackTraceElement.class, "getClassName", "System.Diagnostics.StackFrame", "GetFileName", false);
		mapTransformation(java.lang.StackTraceElement.class, "getMethodName", "System.Diagnostics.StackFrame", "GetMethod", false);
		mapTransformation(java.lang.StackTraceElement.class, "getLineNumber", "System.Diagnostics.StackFrame", "GetFileLineNumber", false);
	}
}
