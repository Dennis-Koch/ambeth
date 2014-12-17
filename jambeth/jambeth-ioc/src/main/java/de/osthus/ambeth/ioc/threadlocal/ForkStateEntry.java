package de.osthus.ambeth.ioc.threadlocal;


public class ForkStateEntry
{
	public final IThreadLocalCleanupBean tlBean;

	public final ThreadLocal<?> valueTL;

	public final ForkableType forkableType;

	public ForkStateEntry(IThreadLocalCleanupBean tlBean, ThreadLocal<?> valueTL, ForkableType forkableType)
	{
		this.tlBean = tlBean;
		this.valueTL = valueTL;
		this.forkableType = forkableType;
	}
}