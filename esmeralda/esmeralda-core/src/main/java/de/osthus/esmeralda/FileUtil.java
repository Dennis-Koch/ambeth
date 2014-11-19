package de.osthus.esmeralda;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class FileUtil implements IFileUtil
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public IList<File> findAllSourceFiles(File[] sourcePath)
	{
		final ArrayList<File> sourceFiles = new ArrayList<File>();

		searchForFiles(sourcePath, new FileFilter()
		{
			@Override
			public boolean accept(File file)
			{
				if (!file.getName().endsWith(".java"))
				{
					return false;
				}
				// if (file.getPath().contains("repackaged"))
				// {
				// return false;
				// }
				sourceFiles.add(file);
				return true;
			}
		});
		return sourceFiles;
	}

	@Override
	public void searchForFiles(File[] baseDirs, FileFilter fileFilter)
	{
		for (File pathItem : baseDirs)
		{
			File rootDir;
			try
			{
				rootDir = pathItem.getCanonicalFile();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			searchForFiles(rootDir, rootDir, fileFilter);
		}
	}

	@Override
	public void searchForFiles(File baseDir, File currFile, FileFilter fileFilter)
	{
		if (currFile == null)
		{
			return;
		}
		if (currFile.isDirectory())
		{
			if ("target".equals(currFile.getName()))
			{
				// skip scanning directory
				return;
			}
			File[] listFiles = currFile.listFiles();
			for (File child : listFiles)
			{
				searchForFiles(baseDir, child, fileFilter);
			}
			return;
		}
		fileFilter.accept(currFile);
	}
}
