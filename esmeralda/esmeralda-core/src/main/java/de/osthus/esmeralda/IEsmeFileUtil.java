package de.osthus.esmeralda;

import java.io.File;
import java.io.FileFilter;

import de.osthus.ambeth.collections.IList;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface IEsmeFileUtil
{

	IList<File> findAllSourceFiles(File[] sourcePath);

	void searchForFiles(File[] baseDirs, FileFilter fileFilter);

	void searchForFiles(File baseDir, File currFile, FileFilter fileFilter);

}