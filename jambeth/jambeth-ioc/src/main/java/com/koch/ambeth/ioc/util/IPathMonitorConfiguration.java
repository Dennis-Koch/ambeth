package com.koch.ambeth.ioc.util;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface IPathMonitorConfiguration
{
	void registerForExists(Path path);

	void registerForFileSystemEvents(Path path, IFileSystemEventListener callback, WatchEvent.Kind<Path>... kinds);

	void unregisterForFileSystemEvents(Path path, IFileSystemEventListener callback, WatchEvent.Kind<Path>... kinds);
}