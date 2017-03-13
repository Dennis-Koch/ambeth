package com.koch.ambeth.testutil;

import java.io.File;
import java.io.IOException;

public class BundleTestUtil
{
	// On the CI server the 'property.file' value is relative to the normal tests. The bundle tests have a different parent folder.
	public static void correctPropertyFilePath() throws IOException
	{
		String fileLocation = System.getProperty("property.file");
		File file = new File(fileLocation);
		if (!file.isAbsolute())
		{
			String workingDir = System.getProperty("user.dir");
			String absoluteFilename = workingDir + "/../../jambeth/jambeth-test/" + fileLocation;
			File newFile = new File(absoluteFilename);
			String canonicalFilename = newFile.getCanonicalPath();
			System.setProperty("property.file", canonicalFilename);
		}
	}
}
