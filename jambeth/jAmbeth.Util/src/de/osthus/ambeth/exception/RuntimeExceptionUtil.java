package de.osthus.ambeth.exception;

import java.lang.reflect.InvocationTargetException;

import de.osthus.ambeth.collections.IdentityHashSet;

public final class RuntimeExceptionUtil
{
	public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

	public static Throwable mask(Throwable e, Class<?>[] exceptionTypes)
	{
		while (e instanceof InvocationTargetException)
		{
			e = ((InvocationTargetException) e).getTargetException();
		}
		if (e instanceof RuntimeException)
		{
			return e;
		}
		for (int a = exceptionTypes.length; a-- > 0;)
		{
			if (exceptionTypes[a].isAssignableFrom(e.getClass()))
			{
				return e;
			}
		}
		MaskingRuntimeException re = new MaskingRuntimeException(e);
		re.setStackTrace(EMPTY_STACK_TRACE);
		return re;
	}

	public static RuntimeException mask(Throwable e, String message)
	{
		if (e instanceof InvocationTargetException)
		{
			return mask(((InvocationTargetException) e).getTargetException(), message);
		}
		if (e instanceof MaskingRuntimeException && e.getMessage() == null)
		{
			return mask(e.getCause(), message);
		}
		MaskingRuntimeException re = new MaskingRuntimeException(message, e);
		re.setStackTrace(EMPTY_STACK_TRACE);
		return re;
	}

	public static RuntimeException mask(Throwable e)
	{
		while (e instanceof InvocationTargetException)
		{
			e = ((InvocationTargetException) e).getTargetException();
		}
		if (e instanceof RuntimeException)
		{
			return (RuntimeException) e;
		}
		MaskingRuntimeException re = new MaskingRuntimeException(e);
		re.setStackTrace(EMPTY_STACK_TRACE);
		return re;
	}

	public static void fillInClientStackTraceIfPossible(Throwable ex)
	{
		if (ex == null)
		{
			return;
		}
		StackTraceElement[] clientStack = Thread.currentThread().getStackTrace();
		IdentityHashSet<Throwable> visitedExceptions = new IdentityHashSet<Throwable>();
		Throwable exToUpdate = ex;
		while (exToUpdate != null && !visitedExceptions.contains(exToUpdate))
		{
			StackTraceElement[] serverStack = exToUpdate.getStackTrace();
			StackTraceElement[] combinedStack = new StackTraceElement[serverStack.length + clientStack.length];
			System.arraycopy(serverStack, 0, combinedStack, 0, serverStack.length);
			System.arraycopy(clientStack, 0, combinedStack, serverStack.length, clientStack.length);
			exToUpdate.setStackTrace(combinedStack);
			visitedExceptions.add(exToUpdate);
			exToUpdate = exToUpdate.getCause();
		}
	}

	public static RuntimeException createEnumNotSupportedException(Enum<?> enumInstance)
	{
		return new EnumNotSupportedException(enumInstance);
	}

	private RuntimeExceptionUtil()
	{
		// Intended blank
	}
}
