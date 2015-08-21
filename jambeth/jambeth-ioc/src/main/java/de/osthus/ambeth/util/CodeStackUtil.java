package de.osthus.ambeth.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.Tuple4KeyHashMap;
import de.osthus.ambeth.ioc.config.AbstractPropertyConfiguration;

public final class CodeStackUtil
{
	public static interface ICodeStackHandle
	{
		void printStackTrace(OutputStream os);

		void printStackTrace(Writer writer);
	}

	private static class CodeStackHandle implements ICodeStackHandle
	{
		private StackTraceElement[] declarationStackTrace;

		private CodeStackHandle(StackTraceElement[] declarationStackTrace)
		{
			this.declarationStackTrace = declarationStackTrace;
		}

		@Override
		public String toString()
		{
			StringWriter sw = new StringWriter();
			printStackTrace(sw);
			return sw.toString();
		}

		@Override
		public void printStackTrace(OutputStream os)
		{
			RuntimeException re = new RuntimeException();
			re.setStackTrace(declarationStackTrace);
			if (os instanceof PrintStream)
			{
				re.printStackTrace((PrintStream) os);
			}
			else
			{
				re.printStackTrace(new PrintStream(os));
			}
		}

		@Override
		public void printStackTrace(Writer writer)
		{
			RuntimeException re = new RuntimeException();
			re.setStackTrace(declarationStackTrace);
			if (writer instanceof PrintWriter)
			{
				re.printStackTrace((PrintWriter) writer);
			}
			else
			{
				re.printStackTrace(new PrintWriter(writer));
			}
		}
	}

	protected static final HashSet<String> ignoreClassNames = new HashSet<String>(0.5f);

	protected static final Tuple4KeyHashMap<String, String, String, Integer, Reference<StackTraceElement>> steReuseMap = new Tuple4KeyHashMap<String, String, String, Integer, Reference<StackTraceElement>>();

	protected static final java.util.concurrent.locks.Lock writeLock = new ReentrantLock();

	static
	{
		ignoreClassNames.add(Thread.class.getName());
		ignoreClassNames.add(CodeStackUtil.class.getName());
		ignoreClassNames.add(CodeStackHandle.class.getName());
		ignoreClassNames.add(AbstractPropertyConfiguration.class.getName());
	}

	public static CodeStackHandle createCodeStackHandle()
	{
		StackTraceElement[] stes = AbstractPropertyConfiguration.getCurrentStackTraceCompact(ignoreClassNames);
		writeLock.lock();
		try
		{
			for (int a = stes.length; a-- > 0;)
			{
				StackTraceElement ste = stes[a];
				Integer ln = Integer.valueOf(ste.getLineNumber());
				Reference<StackTraceElement> existingSteR = steReuseMap.get(ste.getFileName(), ste.getClassName(), ste.getMethodName(), ln);
				StackTraceElement existingSte = null;
				if (existingSteR != null)
				{
					existingSte = existingSteR.get();
				}
				if (existingSte != null)
				{
					stes[a] = existingSte;
					continue;
				}
				steReuseMap.put(ste.getFileName(), ste.getClassName(), ste.getMethodName(), ln, new WeakReference<StackTraceElement>(stes[a]));
			}
		}
		finally
		{
			writeLock.unlock();
		}
		return new CodeStackHandle(stes);
	}
}
