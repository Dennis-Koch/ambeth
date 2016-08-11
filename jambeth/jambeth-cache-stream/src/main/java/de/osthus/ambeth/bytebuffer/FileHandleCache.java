package de.osthus.ambeth.bytebuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IDisposableBean;

public class FileHandleCache implements IFileHandleCache, IDisposableBean
{
	public static final String HANDLE_CLEAR_ALL_CACHES = "handleClearAllCaches";

	protected final SmartCopyMap<FileKey, Reference<RandomAccessFile>> fileToAccessMap = new SmartCopyMap<FileKey, Reference<RandomAccessFile>>();

	protected final HashSet<FileKey> busySet = new HashSet<FileKey>();

	protected final Lock writeLock = new ReentrantLock();

	protected final Condition busyCondition = writeLock.newCondition();

	@Override
	public void destroy() throws Throwable
	{
		closeAllFileHandles();
	}

	public void handleClearAllCaches(ClearAllCachesEvent evt)
	{
		closeAllFileHandles();
	}

	protected void closeAllFileHandles()
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			for (Entry<FileKey, Reference<RandomAccessFile>> entry : fileToAccessMap)
			{
				RandomAccessFile raFile = entry.getValue().get();
				if (raFile == null)
				{
					continue;
				}
				try
				{
					raFile.close();
				}
				catch (IOException e)
				{
					// intended blank
				}
			}
			fileToAccessMap.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public <T> T readOnFile(FileKey fileKey, IFileReadDelegate<T> fileReadDelegate)
	{
		Lock writeLock = this.writeLock;
		HashSet<FileKey> busySet = this.busySet;
		Condition busyCondition = this.busyCondition;
		RandomAccessFile raFile = null;
		writeLock.lock();
		try
		{
			Reference<RandomAccessFile> vtdNavR = fileToAccessMap.get(fileKey);
			if (vtdNavR != null)
			{
				raFile = vtdNavR.get();
			}
			if (raFile == null)
			{
				File file = ((FileKeyImpl) fileKey).getFilePath().toFile();
				if (!file.exists())
				{
					throw new FileNotFoundException("File '" + file.getAbsolutePath() + "' not found");
				}
				raFile = new RandomAccessFile(file, "rw");

				fileToAccessMap.put(fileKey, new SoftReference<RandomAccessFile>(raFile));
			}
			while (!busySet.add(fileKey))
			{
				busyCondition.awaitUninterruptibly();
			}
		}
		catch (FileNotFoundException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.unlock();
		}
		try
		{
			return fileReadDelegate.read(raFile);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.lock();
			try
			{
				busySet.remove(fileKey);
				busyCondition.signalAll();
			}
			finally
			{
				writeLock.unlock();
			}
		}
	}
}
