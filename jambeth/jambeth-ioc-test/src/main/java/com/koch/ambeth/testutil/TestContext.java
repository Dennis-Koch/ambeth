package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-ioc-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LoggerFactory;

public class TestContext implements ITestContext
{
	private static final Pattern pathSeparator = Pattern.compile(Pattern.quote(File.pathSeparator));

	@Property(name = "line.separator")
	protected String nl;

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
