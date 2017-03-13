package com.koch.ambeth.server.helloworld.setup;

import java.io.File;

import org.junit.Test;

import com.koch.ambeth.server.helloworld.AbstractHelloWorldTest;

public class RebuildSchema
{
	public static class DummyTest extends AbstractHelloWorldTest
	{
		@Test
		public void dummyTest()
		{
			// noop
		}
	}

	public static void main(String[] args) throws Exception
	{
		// String propertyFileToUse = args[0];
		String propertyFileToUse = "../jambeth-server-helloworld/src/main/resources/helloworld.properties";

		if (propertyFileToUse.startsWith("/") || propertyFileToUse.contains(":"))
		{
			// is absolute
			System.setProperty("property.file", propertyFileToUse);
		}
		else
		{
			// is relative
			System.setProperty("property.file", new File("").getAbsolutePath() + File.separator + propertyFileToUse);
		}
		com.koch.ambeth.testutil.RebuildSchema.main(args, DummyTest.class, propertyFileToUse);
	}
}
