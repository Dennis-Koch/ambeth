package com.koch.ambeth.core.start;

/*-
 * #%L
 * jambeth-core
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class SystemClasspathInfo implements IClasspathInfo {
	@LogInstance
	private ILogger log;

	@Override
	public IList<URL> getJarURLs() {
		ArrayList<URL> urls = new ArrayList<>();

		String cpString = System.getProperty("java.class.path");
		String separator = System.getProperty("path.separator");
		String[] cpItems = cpString.split(Pattern.quote(separator));

		if (log != null && log.isDebugEnabled()) {
			log.debug("Classpath: " + cpString);
		}

		for (int a = 0, size = cpItems.length; a < size; a++) {
			try {
				URL url = new File(cpItems[a]).toURI().toURL();
				// URL url = new URL("file://" + cpItems[a]);
				urls.add(url);
			}
			catch (MalformedURLException e) {
				throw RuntimeExceptionUtil.mask(e, cpItems[a]);
			}
		}

		return urls;
	}

	@Override
	public Path openAsFile(URL url) throws Throwable {
		return new File(url.getFile()).getAbsoluteFile().toPath();
	}
}
