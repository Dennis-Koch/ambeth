package com.koch.ambeth.extscanner;

import java.io.File;

@FunctionalInterface
public interface IFileFoundDelegate {
	void fileFound(File file, String relativeFilePath);
}
