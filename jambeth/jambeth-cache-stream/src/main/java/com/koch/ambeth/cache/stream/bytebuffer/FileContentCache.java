package com.koch.ambeth.cache.stream.bytebuffer;

/*-
 * #%L
 * jambeth-cache-stream
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

import java.io.RandomAccessFile;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.cache.stream.config.ByteBufferConfigurationConstants;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IdentityWeakHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import sun.nio.ch.DirectBuffer;

@SuppressWarnings("restriction")
public class FileContentCache
		implements IInitializingBean, IDisposableBean, IFileContentCache, Runnable {
	private static class Counter {
		private int counter;

		public int getCounter() {
			return counter;
		}

		public void increase() {
			counter++;
		}

		public void decrease() {
			if (counter > 0) {
				counter--;
			}
		}
	}

	public static final String HANDLE_CLEAR_ALL_CACHES = "handleClearAllCaches";

	@LogInstance
	private ILogger log;

	protected final HashMap<ChunkKey, Reference<ByteBuffer>> fileToContentMap = new HashMap<>();

	protected final IdentityWeakHashMap<ByteBuffer, Counter> contentToUsageCounterMap = new IdentityWeakHashMap<>();

	protected final SmartCopyMap<FileKey, IByteBuffer> fileToVtdNavMap = new SmartCopyMap<>();

	protected int inUseCounter;

	protected final LinkedHashSet<ChunkKey> requestedQueue = new LinkedHashSet<>();

	protected final HashMap<FileKey, Long> fileToLengthMap = new HashMap<>();

	protected final Lock writeLock = new ReentrantLock();

	protected final Condition requestedCondition = writeLock.newCondition();

	protected final Condition newRequestedCondition = writeLock.newCondition();

	protected final Condition indexFinishedCondition = writeLock.newCondition();

	protected FileContentPRC prc;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IClassLoaderProvider classLoaderProvider;

	@Autowired
	protected IFileHandleCache fileHandleCache;

	@Autowired
	protected ILoggerCache loggerCache;

	@Property(name = ByteBufferConfigurationConstants.ChunkSize, defaultValue = ""
			+ (128 * 1024 * 1024))
	protected int virtualChunkSize;

	@Property(name = ByteBufferConfigurationConstants.CleanupCounterThreshold, defaultValue = "10")
	protected int cleanupCounterThreshold;

	@Property(name = ByteBufferConfigurationConstants.FreePhysicalMemoryRatio, defaultValue = "0.1")
	protected double freePhysicalMemoryRatio;

	@Property(name = ByteBufferConfigurationConstants.ChunkPrefetchCount, defaultValue = "1")
	protected int chunkPrefetchCount;

	protected boolean started, terminate;

	protected boolean terminationFinished;

	private final static Random random = new Random();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Throwable {
		prc = new FileContentPRC(log, cleanupCounterThreshold, freePhysicalMemoryRatio) {
			@Override
			protected void doCleanup(ChunkPhantomReference phantom) {
				Lock writeLock = FileContentCache.this.writeLock;
				writeLock.lock();
				try {
					fileToContentMap.remove(phantom.getChunkKey());
				}
				finally {
					writeLock.unlock();
				}
				super.doCleanup(phantom);
			}
		};
	}

	protected void ensureThread() {
		if (started) {
			return;
		}
		started = true;
		Thread thread = new Thread(this);
		thread.setContextClassLoader(classLoaderProvider.getClassLoader());
		thread.setName("FileContentCache ID: " + Math.abs(random.nextInt()));
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void destroy() throws Throwable {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			terminate = true;
			newRequestedCondition.signalAll();
			requestedCondition.signalAll();

			if (started) {
				int seconds = 5;
				Date waitTill = new Date(System.currentTimeMillis() + seconds * 1000l);

				while (!terminationFinished) {
					if (!requestedCondition.awaitUntil(waitTill)) {
						throw new IllegalStateException("Thread did not finish within " + seconds + " seconds");
					}
				}
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	public void handleClearAllCaches(ClearAllCachesEvent evt) {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			fileToContentMap.clear();
			contentToUsageCounterMap.clear();
			fileToVtdNavMap.clear();
			fileToLengthMap.clear();
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void run() {
		Lock writeLock = this.writeLock;
		try {
			Condition newRequestedCondition = this.newRequestedCondition;
			HashMap<ChunkKey, Reference<ByteBuffer>> fileToContentMap = this.fileToContentMap;
			IdentityWeakHashMap<ByteBuffer, Counter> contentToUsageCounterMap = this.contentToUsageCounterMap;
			LinkedHashSet<ChunkKey> requestedQueue = this.requestedQueue;
			while (!terminate) {
				ChunkKey request = null;
				writeLock.lock();
				try {
					if (requestedQueue.isEmpty()) {
						newRequestedCondition.awaitUninterruptibly();
						continue;
					}
					request = requestedQueue.iterator().next();
				}
				finally {
					writeLock.unlock();
				}
				if (request == null) {
					continue;
				}
				prc.checkForCleanup();
				ByteBuffer buffer = handleRequest(request);
				writeLock.lock();
				try {
					prc.queue(new ChunkPhantomReference(buffer, prc.getReferenceQueue(), request));
					requestedQueue.remove(request);
					contentToUsageCounterMap.put(buffer, new Counter());
					Reference<ByteBuffer> bufferR = contentToUsageCounterMap.getWeakReferenceEntry(buffer);
					fileToContentMap.put(request, bufferR);
					requestedCondition.signalAll();
				}
				finally {
					writeLock.unlock();
				}
			}
		}
		finally {
			writeLock.lock();
			try {
				terminationFinished = true;
				requestedCondition.signalAll();
			}
			finally {
				writeLock.unlock();
			}
		}
	}

	protected long getFileLength(FileKey fileKey) {
		Long length = fileToLengthMap.get(fileKey);
		if (length == null) {
			length = Long.valueOf(fileHandleCache.readOnFile(fileKey, new IFileReadDelegate<Long>() {
				@Override
				public Long read(RandomAccessFile raFile) throws Exception {
					return raFile.length();
				}
			}));
			fileToLengthMap.put(fileKey, length);
		}
		return length.longValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IByteBuffer getByteBuffer(FileKey fileKey) {
		IByteBuffer nav = fileToVtdNavMap.get(fileKey);
		if (nav != null) {
			return nav;
		}
		long length = getFileLength(fileKey);
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			while (fileToVtdNavMap.containsKey(fileKey) && fileToVtdNavMap.get(fileKey) == null) {
				// Index already in progress
				indexFinishedCondition.awaitUninterruptibly();
			}
			nav = fileToVtdNavMap.get(fileKey);
			if (nav != null) {
				return nav;
			}
			fileToVtdNavMap.put(fileKey, null);
		}
		finally {
			writeLock.unlock();
		}
		boolean success = false;
		try {
			nav = new LargeByteBuffer(this, fileKey, length, virtualChunkSize);
			success = true;
			return nav;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			writeLock.lock();
			try {
				if (!success) {
					fileToVtdNavMap.remove(fileKey);
				}
				else {
					fileToVtdNavMap.put(fileKey, nav);
				}
				indexFinishedCondition.signalAll();
			}
			finally {
				writeLock.unlock();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("restriction")
	@Override
	public void releaseByteBuffer(ByteBuffer byteBuffer) {
		if (!(byteBuffer instanceof DirectBuffer)) {
			return;
		}
		DirectBuffer directBuffer = (DirectBuffer) byteBuffer;
		ByteBuffer attachment = (ByteBuffer) directBuffer.attachment();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			decreaseUsage(attachment);
			if (Math.min(contentToUsageCounterMap.size(), fileToContentMap.size())
					- inUseCounter >= cleanupCounterThreshold) {
				prc.cleanup();
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ByteBuffer getContent(FileKey fileKey, long position) {
		long length = getFileLength(fileKey);
		ByteBuffer[] content = getContent(fileKey, position,
				Math.min(length - position, virtualChunkSize));
		if (content.length == 0) {
			return null;
		}
		if (content.length != 1) {
			throw new IllegalStateException("Must never happen");
		}
		return content[0];
	}

	protected void decreaseUsage(ByteBuffer buffer) {
		Counter counter = contentToUsageCounterMap.get(buffer);
		if (counter.getCounter() == 0) {
			return;
		}
		counter.decrease();
		if (counter.getCounter() == 0) {
			inUseCounter--;
		}
	}

	protected void increaseUsage(ByteBuffer buffer) {
		Counter counter = contentToUsageCounterMap.get(buffer);
		counter.increase();
		if (counter.getCounter() == 1) {
			inUseCounter++;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ByteBuffer[] getContent(FileKey fileKey, long position, long length) {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			ArrayList<ByteBuffer> bufferList = new ArrayList<>();
			long currentPosition = position;
			long currentLength = length;
			while (currentLength > 0) {
				long paddedPosition = (currentPosition / virtualChunkSize) * virtualChunkSize;
				int deltaPosition = (int) (currentPosition - paddedPosition);

				ChunkKey key = new ChunkKey(fileKey, paddedPosition);
				ByteBuffer buffer = requestChunk(key, virtualChunkSize);

				int oldPosition = buffer.position();
				int oldLimit = buffer.limit();
				try {
					buffer.position(deltaPosition);
					int limit = (int) Math.min(deltaPosition + currentLength, virtualChunkSize);
					buffer.limit(limit);
					ByteBuffer slice = buffer.slice();

					increaseUsage(buffer);

					bufferList.add(slice);

					// Move cursor to the next 2gig buffer
					currentPosition += virtualChunkSize;
					currentLength -= virtualChunkSize;
				}
				finally {
					buffer.limit(oldLimit);
					buffer.position(oldPosition);
				}
			}
			return bufferList.toArray(ByteBuffer.class);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			writeLock.unlock();
		}
	}

	protected ByteBuffer requestChunk(ChunkKey key, long chunkSize) throws TimeoutException {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			ensureThread();
			long paddedPosition = key.getPaddedPosition();
			// First we look whether the preceeding chunk is cached. If it exists we expect a serialized
			// access and prefetch following chunks
			if (paddedPosition == 0) {
				// First chunk has no preceeding chunk so no prefetch intended. We just fetch the requested
				// chunk
				return requestChunk(key, true);
			}
			ChunkKey previousChunkKey = new ChunkKey(key.getFileKey(), paddedPosition - chunkSize);
			if (tryGetByteBuffer(previousChunkKey) == null) {
				// Previous buffer does not exist. We assume a random access and do not prefetch
				return requestChunk(key, true);
			}
			// Prefetch
			long length = getFileLength(key.getFileKey());
			ByteBuffer requestedBuffer = requestChunk(key, false);
			for (int a = 0, size = chunkPrefetchCount; a < size; a++) {
				long prefetchPosition = paddedPosition + chunkSize * (a + 1);
				if (length > prefetchPosition) {
					// File is big enough that the next chunk has a size of at least 1. So it exists and we
					// therefore prefetch it
					ChunkKey prefetchChunkKey = new ChunkKey(key.getFileKey(), prefetchPosition);
					requestChunk(prefetchChunkKey, false);
				}
			}
			if (requestedBuffer != null) {
				return requestedBuffer;
			}
			return requestChunk(key, true);
		}
		finally {
			writeLock.unlock();
		}
	}

	protected ByteBuffer tryGetByteBuffer(ChunkKey key) {
		Reference<ByteBuffer> contentR = fileToContentMap.get(key);
		if (contentR != null) {
			return contentR.get();
		}
		return null;
	}

	protected ByteBuffer requestChunk(ChunkKey key, boolean waitForResult) throws TimeoutException {
		int seconds = 60;
		Date waitTill = waitForResult ? new Date(System.currentTimeMillis() + seconds * 1000l) : null;
		while (true) {
			ByteBuffer buffer = tryGetByteBuffer(key);
			if (buffer != null) {
				return buffer;
			}
			if (requestedQueue.add(key)) {
				newRequestedCondition.signal();
			}
			if (waitForResult) {
				try {
					if (requestedCondition.awaitUntil(waitTill)) {
						continue;
					}
				}
				catch (InterruptedException e) {
					continue;
				}
				throw new TimeoutException("Request could not be served within " + seconds + " seconds");
			}
			return null;
		}
	}

	protected ByteBuffer handleRequest(final ChunkKey chunkKey) {
		final long length = getFileLength(chunkKey.getFileKey());
		return fileHandleCache.readOnFile(chunkKey.getFileKey(), new IFileReadDelegate<ByteBuffer>() {
			@Override
			public ByteBuffer read(RandomAccessFile raFile) throws Exception {
				long paddedPosition = chunkKey.getPaddedPosition();
				// Do not resize the file unintentionally because of a too large maxMappedSize
				int maxMappedSize = (int) Math.min(length - paddedPosition, virtualChunkSize);
				if (maxMappedSize <= 0) {
					return null;
				}
				FileChannel channel = raFile.getChannel();
				// ByteBuffer buffer = ByteBuffer.allocate(maxMappedSize);
				// channel.position(paddedPosition);
				// channel.read(buffer);
				// return buffer;
				return channel.map(MapMode.READ_ONLY, paddedPosition, maxMappedSize);
			}
		});
	}
}
