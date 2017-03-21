package com.koch.ambeth.util.proxy;

/*-
 * #%L
 * jambeth-util
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

import java.util.Arrays;

public class ProxyTypeKey {
	private final Class<?>[] interfaces;

	private final Class<?> baseType;

	public ProxyTypeKey(Class<?> baseType, Class<?>[] interfaces) {
		this.interfaces = interfaces;
		this.baseType = baseType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProxyTypeKey)) {
			return false;
		}
		ProxyTypeKey other = (ProxyTypeKey) obj;
		return Arrays.equals(interfaces, other.interfaces) && baseType.equals(other.baseType);
	}

	@Override
	public int hashCode() {
		return baseType.hashCode() ^ Arrays.hashCode(interfaces);
	}
}
