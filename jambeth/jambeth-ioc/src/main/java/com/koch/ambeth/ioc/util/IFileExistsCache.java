package com.koch.ambeth.ioc.util;

import java.nio.file.Path;

public interface IFileExistsCache
{

	boolean exists(Path path);

}