package com.koch.ambeth.ioc.util;

/*-
 * #%L
 * jambeth-ioc
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

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityWeakHashMap;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.AbstractFileVisitor;

public class FileExistsCache
		implements IDisposableBean, Runnable, IFileExistsCache, IPathMonitorConfiguration {
	@LogInstance
	private ILogger log;

	@SuppressWarnings("unchecked")
	private static final WatchEvent.Kind<Path>[] CREATE_AND_DELETE = new WatchEvent.Kind[] {
			StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE };

	protected final IdentityWeakHashMap<FileSystem, WatchService> fileSystemToWatchServiceMap = new IdentityWeakHashMap<>();

	protected final Tuple2KeyHashMap<WatchService, Path, Object> watchServiceMap = new Tuple2KeyHashMap<>();

	protected final IdentityWeakHashMap<FileSystem, ISet<String>> existingPathsMap = new IdentityWeakHashMap<>();

	protected final java.util.concurrent.locks.Lock readLock;

	protected final java.util.concurrent.locks.Lock writeLock;

	protected final Condition newEntryCond;

	protected volatile Thread thread;

	protected volatile boolean disposed;

	private IFileSystemEventListener existsDelegate = new IFileSystemEventListener() {
		@SuppressWarnings("rawtypes")
		@Override
		public void entryChanged(FileSystem fileSystem, WatchService watchService, Path path, Kind kind,
				Path filename) {
			FileExistsCache.this.entryChanged(fileSystem, watchService, path, kind, filename);
		}
	};

	public FileExistsCache() {
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
		newEntryCond = writeLock.newCondition();
	}

	@Override
	public void destroy() throws Throwable {
		if (thread == null) {
			return;
		}
		disposed = true;
		writeLock.lock();
		try {
			for (Entry<FileSystem, WatchService> entry : fileSystemToWatchServiceMap) {
				FileSystem fileSystem = entry.getKey();
				if (fileSystem == null) {
					continue;
				}
				entry.getValue().close();
			}
			fileSystemToWatchServiceMap.clear();
			watchServiceMap.clear();
			existingPathsMap.clear();
		}
		finally {
			writeLock.unlock();
		}
		thread.interrupt();
	}

	@Override
	public boolean exists(Path path) {
		FileSystem fileSystem = path.getFileSystem();
		readLock.lock();
		try {
			ISet<String> existingPaths = existingPathsMap.get(fileSystem);
			if (existingPaths == null) {
				return Files.exists(path);
			}
			return existingPaths.contains(path.toAbsolutePath().normalize().toString());
		}
		finally {
			readLock.unlock();
		}
	}

	@Override
	public void registerForExists(Path path) {
		registerForFileSystemEvents(path, existsDelegate, CREATE_AND_DELETE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerForFileSystemEvents(Path path, IFileSystemEventListener callback,
			WatchEvent.Kind<Path>... kinds) {
		if (disposed) {
			throw new IllegalStateException("Bean already disposed");
		}
		writeLock.lock();
		try {
			FileSystem fileSystem = path.getFileSystem();
			WatchService watchService = fileSystemToWatchServiceMap.get(fileSystem);
			if (watchService == null) {
				watchService = fileSystem.newWatchService();
				fileSystemToWatchServiceMap.put(fileSystem, watchService);
			}
			ISet<String> existingPaths = existingPathsMap.get(fileSystem);
			if (existingPaths == null) {
				existingPaths = new HashSet<>();
				existingPathsMap.put(fileSystem, existingPaths);
			}
			parseDirectory(path, watchService, existingPaths, callback, kinds);
			if (thread == null) {
				thread = new Thread(this);
				thread.setDaemon(true);
				thread.setName(getClass().getSimpleName());
				thread.start();
			}
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			writeLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unregisterForFileSystemEvents(Path path, IFileSystemEventListener callback,
			Kind<Path>... kinds) {
		// TODO Not yet implemented
	}

	private void parseDirectory(Path path, final WatchService watchService,
			final ISet<String> existingPaths, final IFileSystemEventListener callback,
			final WatchEvent.Kind<?>[] watchEventKinds) {
		try {
			Files.walkFileTree(path.toAbsolutePath().normalize(), new AbstractFileVisitor() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
						throws IOException {
					registerPath(watchService, existingPaths, callback, watchEventKinds, dir);
					return super.preVisitDirectory(dir, attrs);
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					existingPaths.add(file.toString());
					return super.visitFile(file, attrs);
				}
			});
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (!disposed) {
			writeLock.lock();
			try {
				for (Entry<FileSystem, WatchService> entry : fileSystemToWatchServiceMap) {
					FileSystem fileSystem = entry.getKey();
					if (fileSystem == null) {
						continue;
					}
					WatchService watchService = entry.getValue();
					WatchKey key;
					if (fileSystemToWatchServiceMap.size() == 1) {
						writeLock.unlock();
						try {
							key = watchService.take();
						}
						catch (ClosedWatchServiceException e) {
							continue;
						}
						catch (InterruptedException e) {
							Thread.interrupted();
							continue;
						}
						finally {
							writeLock.lock();
						}
					}
					else {
						try {
							key = entry.getValue().poll(0, TimeUnit.MILLISECONDS);
						}
						catch (ClosedWatchServiceException e) {
							continue;
						}
						catch (InterruptedException e) {
							continue;
						}
					}
					Watchable watchable = key.watchable();
					for (WatchEvent<?> event : key.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();

						if (kind == StandardWatchEventKinds.OVERFLOW) {
							continue;
						}

						// The filename is the context of the event.
						WatchEvent<Path> ev = (WatchEvent<Path>) event;
						Path filename = ev.context();
						Path path = (Path) watchable;
						Object callbacks = watchServiceMap.get(watchService, path);
						if (callbacks == null) {
							continue;
						}
						if (callbacks instanceof IFileSystemEventListener) {
							try {
								((IFileSystemEventListener) callbacks).entryChanged(fileSystem, watchService, path,
										kind, filename);
							}
							catch (Throwable e) {
								log.error(e);
							}
						}
						else {
							for (IFileSystemEventListener existingCallback : (IFileSystemEventListener[]) callbacks) {
								try {
									existingCallback.entryChanged(fileSystem, watchService, path, kind, filename);
								}
								catch (Throwable e) {
									log.error(e);
								}
							}
						}
					}

					// Reset the key -- this step is critical if you want to receive
					// further watch events. If the key is no longer valid, the directory
					// is inaccessible so exit the loop.
					key.reset();
				}
				if (fileSystemToWatchServiceMap.size() > 1) {
					try {
						newEntryCond.await(100, TimeUnit.MILLISECONDS);
					}
					catch (InterruptedException e) {
						Thread.interrupted();
						continue;
					}
				}
			}
			finally {
				writeLock.unlock();
			}
		}
	}

	protected boolean registerPath(final WatchService watchService, final ISet<String> existingPaths,
			final IFileSystemEventListener callback, final WatchEvent.Kind<?>[] watchEventKinds, Path dir)
			throws IOException {
		Object callbacks = watchServiceMap.get(watchService, dir);
		if (callbacks != null) {
			if (callbacks instanceof IFileSystemEventListener) {
				if (callbacks == callback) {
					return false;
				}
			}
			else {
				for (IFileSystemEventListener existingCallback : (IFileSystemEventListener[]) callbacks) {
					if (existingCallback == callback) {
						return false;
					}
				}
			}
		}
		else {
			dir.register(watchService, watchEventKinds);
			newEntryCond.signal();
		}
		if (callbacks != null) {
			if (callbacks instanceof IFileSystemEventListener) {
				watchServiceMap.put(watchService, dir,
						new IFileSystemEventListener[] { (IFileSystemEventListener) callbacks, callback });
			}
			else {
				IFileSystemEventListener[] arr = (IFileSystemEventListener[]) callbacks;
				IFileSystemEventListener[] copy = java.util.Arrays.copyOf(arr, arr.length + 1);
				copy[copy.length - 1] = callback;
				watchServiceMap.put(watchService, dir, copy);
			}
		}
		else {
			watchServiceMap.put(watchService, dir, callback);
		}
		existingPaths.add(dir.toString());
		return true;
	}

	@SuppressWarnings("rawtypes")
	protected void entryChanged(FileSystem fileSystem, WatchService watchService, Path parent,
			WatchEvent.Kind kind, Path filename) {
		Path path = parent.resolve(filename);
		ISet<String> set = existingPathsMap.get(fileSystem);
		if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
			set.remove(path.toString());
		}
		else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
			if (Files.isDirectory(path)) {
				parseDirectory(path, watchService, set, existsDelegate, CREATE_AND_DELETE);
			}
			else if (Files.exists(path)) {
				set.add(path.toString());
			}
		}
		else {
			System.out.println("jdkfjsdkfjsf");
		}
	}
}
