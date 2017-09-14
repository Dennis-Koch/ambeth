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

import org.objectweb.asm.Type;

public class MethodKeyOfType {
	protected final String methodName;

	protected final Type returnType;

	protected final Type[] parameterTypes;

	public MethodKeyOfType(String methodName, Type returnType, Type[] parameterTypes) {
		this.methodName = methodName;
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	public String getMethodName() {
		return methodName;
	}

	public Type getReturnType() {
		return returnType;
	}

	public Type[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public int hashCode() {
		return methodName.hashCode() ^ parameterTypes.length ^ returnType.hashCode();
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
		MethodKeyOfType other = (MethodKeyOfType) obj;
		if (!Objects.equals(methodName, other.methodName)
				|| !Objects.equals(returnType, other.returnType)) {
			return false;
		}
		if (!Arrays.equals(parameterTypes, other.parameterTypes)) {
			return false;
		}
		return true;
	}
}
