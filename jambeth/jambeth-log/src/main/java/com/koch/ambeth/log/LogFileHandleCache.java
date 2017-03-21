package com.koch.ambeth.log;

/*-
 * #%L
 * jambeth-log
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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

import com.koch.ambeth.util.collections.WeakValueHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

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
