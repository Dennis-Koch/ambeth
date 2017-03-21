package com.koch.ambeth.cache.stream.bytebuffer;

/*-
 * #%L
 * jambeth-cache-stream
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

import java.nio.file.Path;

import com.koch.ambeth.util.IImmutableType;
import com.koch.ambeth.util.IPrintable;

public class FileKeyImpl implements FileKey, IImmutableType, IPrintable {
	private final Path filePath;

	public FileKeyImpl(Path filePath) {
		this.filePath = filePath;
	}

	public Path getFilePath() {
		return filePath;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ filePath.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FileKeyImpl other = (FileKeyImpl) obj;
		return filePath.equals(other.filePath);
	}

	@Override
	public String toString() {
		return filePath.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(filePath);
	}
}
