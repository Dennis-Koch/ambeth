package com.koch.ambeth.util;

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
import java.util.Objects;

public class MethodKey implements IPrintable {
	protected final String methodName;

	protected final Class<?>[] parameterTypes;

	public MethodKey(String methodName, Class<?>[] parameterTypes) {
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public int hashCode() {
		return methodName.hashCode() ^ parameterTypes.length;
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
		MethodKey other = (MethodKey) obj;
		if (!Objects.equals(methodName, other.methodName)) {
			return false;
		}
		if (!Arrays.equals(parameterTypes, other.parameterTypes)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("MethodKey: ").append(methodName).append('(');
		for (int a = 0, size = parameterTypes.length; a < size; a++) {
			if (a > 0) {
				sb.append(", ");
			}
			sb.append(parameterTypes[a].getName());
		}
		sb.append(')');
	}
}
