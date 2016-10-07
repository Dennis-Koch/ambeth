package de.osthus.ambeth.util;

import java.nio.file.Path;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface IFileExistsCache
{

	boolean exists(Path path);

}