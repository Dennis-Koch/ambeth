package de.osthus.esmeralda.misc;

import java.io.File;
import java.io.FileFilter;

import de.osthus.ambeth.collections.IList;

public interface IEsmeFileUtil
{
	IList<File> findAllSourceFiles(File[] sourcePath);

	void searchForFiles(File[] baseDirs, FileFilter fileFilter);

	void searchForFiles(File baseDir, File currFile, FileFilter fileFilter);

	void updateFile(String newFileContent, File targetFile);
}