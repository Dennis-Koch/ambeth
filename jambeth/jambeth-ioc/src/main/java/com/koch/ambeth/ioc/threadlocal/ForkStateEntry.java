package com.koch.ambeth.ioc.threadlocal;

public class ForkStateEntry
{
	public final IThreadLocalCleanupBean tlBean;

	public final String fieldName;

	public final ThreadLocal<?> valueTL;

	public final ForkableType forkableType;

	public final IForkProcessor forkProcessor;

	public ForkStateEntry(IThreadLocalCleanupBean tlBean, String fieldName, ThreadLocal<?> valueTL, ForkableType forkableType, IForkProcessor forkProcessor)
	{
		this.tlBean = tlBean;
		this.fieldName = fieldName;
		this.valueTL = valueTL;
		this.forkableType = forkableType;
		this.forkProcessor = forkProcessor;
	}
}