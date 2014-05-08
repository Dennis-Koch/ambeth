package de.osthus.ambeth.testutil;

import java.io.File;

public interface ITestContext
{
	File getContextFile(String fileName);

	File getContextFile(String fileName, Class<?> testClass);
}
