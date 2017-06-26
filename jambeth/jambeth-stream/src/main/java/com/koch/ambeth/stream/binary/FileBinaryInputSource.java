package com.koch.ambeth.stream.binary;

/*-
 * #%L
 * jambeth-stream
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class FileBinaryInputSource implements IBinaryInputSource {
	protected final Path path;

	public FileBinaryInputSource(Path path) {
		this.path = path;
	}

	@Override
	public IInputStream deriveInputStream() {
		return createIInputStream();
	}

	@Override
	public InputStreamToBinaryInputStream deriveBinaryInputStream() {
		return createIInputStream();
	}

	private InputStreamToBinaryInputStream createIInputStream() {
		InputStream is = null;
		try {
			is = Files.newInputStream(path);
			return new InputStreamToBinaryInputStream(new BufferedInputStream(is));
		}
		catch (Throwable e) {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException e1) {
					// intended blank
				}
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
