package de.osthus.ambeth.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.WeakValueHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class LogFileHandleCache
{
	public static class LoggerStream
	{
		private final Path logfile;

		private Writer writer;

		public final Lock writeLock = new ReentrantLock();

		public LoggerStream(Path logfile)
		{
			this.logfile = logfile;
		}

		public Writer getWriter()
		{
			if (writer == null)
			{
				reopen();
			}
			return writer;
		}

		public void reopen()
		{
			writeLock.lock();
			try
			{
				Writer oldWriter = writer;
				writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(logfile, StandardOpenOption.CREATE, StandardOpenOption.APPEND),
						Charset.forName("UTF-8")));
				if (oldWriter != null)
				{
					try
					{
						oldWriter.close();
					}
					catch (IOException e)
					{
						// intended blank
					}
				}
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			finally
			{
				writeLock.unlock();
			}
		}
	}

	private static final WeakValueHashMap<String, LoggerStream> handleToWriterMap = new WeakValueHashMap<String, LoggerStream>();

	private static final Lock writeLock = new ReentrantLock();

	public static LoggerStream getSharedWriter(Path logfile)
	{
		if (logfile == null)
		{
			return null;
		}
		logfile = logfile.toAbsolutePath().normalize();
		writeLock.lock();
		try
		{
			LoggerStream entry = handleToWriterMap.get(logfile.toString());
			if (entry != null)
			{
				return entry;
			}
			if (!Files.exists(logfile.getParent()))
			{
				Files.createDirectories(logfile.getParent());
			}
			entry = new LoggerStream(logfile);
			handleToWriterMap.put(logfile.toString(), entry);
			return entry;
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
