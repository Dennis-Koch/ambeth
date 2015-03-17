package de.osthus.ambeth.extscanner;

import java.io.File;

public interface IFileFoundDelegate
{
	void fileFound(File file, String relativeFilePath);
}
