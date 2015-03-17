package de.osthus.esmeralda.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class EsmeFileUtil implements IEsmeFileUtil
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

	@Override
	public void updateFile(String newFileContent, File targetFile)
	{
		if (targetFile.exists())
		{
			StringBuilder existingFileContent = readFileFully(targetFile);
			if (existingFileContent.toString().equals(newFileContent))
			{
				if (log.isDebugEnabled())
				{
					log.debug("File is already up-to-date: " + targetFile);
				}
				return;
			}
			if (log.isInfoEnabled())
			{
				log.info("Updating file: " + targetFile);
			}
		}
		else
		{
			if (log.isInfoEnabled())
			{
				log.info("Creating file: " + targetFile);
			}
		}
		try (Writer fileWriter = new OutputStreamWriter(new FileOutputStream(targetFile), "UTF-8"))
		{
			fileWriter.append(newFileContent);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected StringBuilder readFileFully(File file)
	{
		try
		{
			StringBuilder sb = new StringBuilder((int) file.length());
			BufferedReader rd = new BufferedReader(new FileReader(file));
			try
			{
				int oneByte;
				while ((oneByte = rd.read()) != -1)
				{
					sb.append((char) oneByte);
				}
				return sb;
			}
			finally
			{
				rd.close();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
