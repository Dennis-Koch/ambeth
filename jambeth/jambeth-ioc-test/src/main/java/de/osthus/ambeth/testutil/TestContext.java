package de.osthus.ambeth.testutil;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;

public class TestContext implements ITestContext
{
	private static final String nl = System.getProperty("line.separator");

	private static final Pattern pathSeparator = Pattern.compile(File.pathSeparator);

	protected final AmbethIocRunner runner;

	public TestContext(AmbethIocRunner runner)
	{
		this.runner = runner;
	}

	@Override
	public File getContextFile(String fileName)
	{
		Class<?> testClass = runner.getTestClass().getJavaClass();
		return getContextFile(fileName, testClass);
	}

	@Override
	public File getContextFile(String fileName, Class<?> testClass)
	{
		File file = null;
		File tempFile = new File(fileName);
		if (tempFile.canRead())
		{
			file = tempFile;
		}
		if (file == null)
		{
			String callingNamespace = ((Class<?>) testClass).getPackage().getName();
			String relativePath = fileName.startsWith("/") ? "." + fileName : callingNamespace.replace(".", File.separator) + File.separator + fileName;
			String[] classPaths = pathSeparator.split(System.getProperty("java.class.path"));
			for (int i = 0; i < classPaths.length; i++)
			{
				tempFile = new File(classPaths[i], relativePath);
				if (tempFile.canRead())
				{
					file = tempFile;
					break;
				}
			}
			if (file == null)
			{
				Pattern fileSuffixPattern = Pattern.compile(".+\\.(?:[^\\.]*)");
				Matcher matcher = fileSuffixPattern.matcher(relativePath);
				if (!matcher.matches())
				{
					relativePath += ".sql";
					for (int i = 0; i < classPaths.length; i++)
					{
						tempFile = new File(classPaths[i], relativePath);
						if (tempFile.canRead())
						{
							file = tempFile;
							break;
						}
					}
				}
			}
			if (file == null && !fileName.startsWith("/"))
			{
				// Path is not with root-slash specified. Try to add this before giving up:
				return getContextFile("/" + fileName, testClass);
			}
			if (file == null)
			{
				ILogger log = LoggerFactory.getLogger(testClass);
				if (log.isWarnEnabled())
				{
					String error = "Cannot find '" + relativePath + "' in class path:" + nl;
					Arrays.sort(classPaths);
					for (int i = 0; i < classPaths.length; i++)
					{
						error += "\t" + classPaths[i] + nl;
					}
					log.warn(error);
				}
				return null;
			}
		}

		ILogger log = LoggerFactory.getLogger(testClass);

		if (log.isDebugEnabled())
		{
			log.debug("Resolved test context file: " + file.getAbsolutePath());
		}
		return file;
	}
}