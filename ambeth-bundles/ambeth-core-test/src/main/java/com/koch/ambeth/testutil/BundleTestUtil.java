package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-core-test
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
import java.io.IOException;

public class BundleTestUtil {
	// On the CI server the 'property.file' value is relative to the normal tests. The bundle tests
	// have a different parent folder.
	public static void correctPropertyFilePath() throws IOException {
		String fileLocation = System.getProperty("property.file");
		if (fileLocation == null) {
			return;
		}
		File file = new File(fileLocation);
		if (!file.isAbsolute()) {
			String workingDir = System.getProperty("user.dir");
			String absoluteFilename = workingDir + "/../../jambeth/jambeth-test/" + fileLocation;
			File newFile = new File(absoluteFilename);
			String canonicalFilename = newFile.getCanonicalPath();
			System.setProperty("property.file", canonicalFilename);
		}
	}
}
