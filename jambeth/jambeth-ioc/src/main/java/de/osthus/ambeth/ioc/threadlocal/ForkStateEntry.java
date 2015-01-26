package de.osthus.ambeth.ioc.threadlocal;

import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;

public class ForkStateEntry extends ReentrantLock
{
	private static final long serialVersionUID = 7116007903541849497L;

	public final IThreadLocalCleanupBean tlBean;

	public final String fieldName;

	public final ThreadLocal<?> valueTL;

	public final ForkableType forkableType;

	public final IForkProcessor forkProcessor;

	public ArrayList<Object> forkedValues;

	public ForkStateEntry(IThreadLocalCleanupBean tlBean, String fieldName, ThreadLocal<?> valueTL, ForkableType forkableType, IForkProcessor forkProcessor)
	{
		this.tlBean = tlBean;
		this.fieldName = fieldName;
		this.valueTL = valueTL;
		this.forkableType = forkableType;
		this.forkProcessor = forkProcessor;
	}
}