package com.koch.ambeth.ioc.util;

/*-
 * #%L
 * jambeth-ioc
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

public class Strong2Key<V> {
	protected final V extension;

	protected final ConversionKey key;

	public Strong2Key(V extension, ConversionKey key) {
		this.extension = extension;
		this.key = key;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Strong2Key)) {
			return false;
		}
		Strong2Key<?> other = (Strong2Key<?>) obj;
		return extension == other.extension && key.equals(other.key);
	}

	public ConversionKey getKey() {
		return key;
	}

	public V getExtension() {
		return extension;
	}

	@Override
	public int hashCode() {
		return extension.hashCode() ^ key.hashCode();
	}
}
