package de.osthus.ambeth.training.travelguides.setup;

import java.io.File;

import de.osthus.ambeth.testutil.RebuildSchema;

/**
 * This is used to rebuild a database schema.
 */
public class RebuildSchemaGuideBook
{
	private RebuildSchemaGuideBook()
	{
		// utility class, not to be instantiated
	}

	public static void main(String[] args) throws Exception
	{
		// String propertyFileToUse = args[0];
		String propertyFileToUse = "./src/main/resources/application.properties";

		if (!propertyFileToUse.startsWith("/") && !propertyFileToUse.contains(":"))
		{
			// relative path, add prefix of current directory:
			String currDirectory = new File("").getAbsolutePath();
			propertyFileToUse = currDirectory + File.separator + propertyFileToUse;
		}
		System.setProperty("property.file", propertyFileToUse);

		RebuildSchema.main(args, DummyTest.class, propertyFileToUse);
	}
}
