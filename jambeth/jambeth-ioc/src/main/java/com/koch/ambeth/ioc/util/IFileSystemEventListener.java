package com.koch.ambeth.ioc.util;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;

public interface IFileSystemEventListener
{
	void entryChanged(FileSystem fileSystem, WatchService watchService, Path parent, WatchEvent.Kind<?> kind, Path filename);
}
