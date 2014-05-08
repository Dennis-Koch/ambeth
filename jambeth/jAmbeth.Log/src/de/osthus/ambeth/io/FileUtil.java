package de.osthus.ambeth.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Pattern;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;

public class FileUtil
{
	private static final Pattern CONFIG_SEPARATOR = Pattern.compile(";");

	private static final Pattern PATH_SEPARATOR = Pattern.compile(File.pathSeparator);

	protected FileUtil()
	{
		// Intended blank
	}

	public static String[] splitConfigFileNames(String fileNames)
	{
		String[] splittedfileNames = CONFIG_SEPARATOR.split(fileNames);
		return splittedfileNames;
	}

	public static InputStream[] openFileStreams(String fileNames)
	{
		InputStream[] fileStreams = openFileStreams(fileNames, null);
		return fileStreams;
	}

	public static InputStream[] openFileStreams(String fileNames, ILogger log)
	{
		String[] splittedfileNames = splitConfigFileNames(fileNames);
		InputStream[] fileStreams = openFileStreams(splittedfileNames);
		return fileStreams;
	}

	public static InputStream[] openFileStreams(String... fileNames)
	{
		InputStream[] inputStreams = openFileStreams(fileNames, false, null);
		return inputStreams;
	}

	public static InputStream[] openFileStreams(String[] fileNames, ILogger log)
	{
		return openFileStreams(fileNames, false, log);
	}

	public static InputStream[] openFileStreams(String[] fileNames, boolean ignoreEmptyNames, ILogger log)
	{
		InputStream[] inputStreams = new InputStream[fileNames.length];

		for (int i = fileNames.length; i-- > 0;)
		{
			String fileName = fileNames[i];
			if (fileName == null || fileName.isEmpty())
			{
				continue;
			}

			InputStream inputStream = null;
			Exception original = null;
			try
			{
				inputStream = openFileStream(fileName, log);
			}
			catch (IllegalArgumentException e)
			{
				// inputStream is null. openFileStream() threw this exception, but we could give more informations.
				original = e;
			}
			if (inputStream == null)
			{
				String combinesFileNames = combine(fileNames);
				throw new IllegalArgumentException(String.format("File source '%s' not found in filesystem and classpath. Filenames: '%s'", fileName,
						combinesFileNames), original);
			}
			inputStreams[i] = inputStream;
		}

		return inputStreams;
	}

	public static InputStream openFileStream(String fileName)
	{
		InputStream inputStream = openFileStream(fileName, null);
		return inputStream;
	}

	public static InputStream openFileStream(String fileName, ILogger log)
	{
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		if (inputStream != null && log != null && log.isDebugEnabled())
		{
			log.debug("Using stream resource '" + fileName + "'");
		}
		else
		{
			File file = openFile(fileName, log);
			if (file != null)
			{
				try
				{
					inputStream = new FileInputStream(file);
				}
				catch (FileNotFoundException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}

		if (inputStream == null)
		{
			throw new IllegalArgumentException(String.format("File source '%s' not found in filesystem and classpath.", fileName));
		}

		return inputStream;
	}

	public static File openFile(String fileName)
	{
		return openFile(fileName, null);
	}

	public static File openFile(String fileName, ILogger log)
	{
		File file = tryFileSystemPosition(fileName, log);
		if (file != null)
		{
			return file;
		}

		String pathName = null;
		String[] classPaths = PATH_SEPARATOR.split(System.getProperty("java.class.path"));
		for (int i = 0; i < classPaths.length; i++)
		{
			pathName = classPaths[i];
			file = tryFileSystemPosition(pathName, fileName, log);
			if (file != null)
			{
				return file;
			}
		}
		return null;
	}

	protected static File tryFileSystemPosition(String fileName, ILogger log)
	{
		return tryFileSystemPosition(null, fileName, log);
	}

	protected static File tryFileSystemPosition(String pathName, String fileName, ILogger log)
	{
		File file;
		file = pathName == null || pathName.isEmpty() ? new File(fileName) : new File(pathName, fileName);
		if (file.canRead())
		{
			if (log != null && log.isDebugEnabled())
			{
				log.debug(String.format("Using file resource '%s'", file.getAbsolutePath()));
			}
			return file;
		}
		return null;
	}

	protected static String combine(String[] strings)
	{
		if (strings == null || strings.length == 0)
		{
			return "";
		}
		else if (strings.length == 1)
		{
			return strings[0];
		}
		else
		{
			StringBuilder sb = new StringBuilder(strings[0]);
			for (int i = 1; i < strings.length; i++)
			{
				sb.append(", ").append(strings[i]);
			}
			return sb.toString();
		}
	}
}
