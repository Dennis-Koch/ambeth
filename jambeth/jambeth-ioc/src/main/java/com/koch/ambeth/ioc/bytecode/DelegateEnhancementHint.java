package com.koch.ambeth.ioc.bytecode;

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

import com.koch.ambeth.util.EqualsUtil;

public class DelegateEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint {
	private final Class<?> type;

	private final Class<?> parameterType;

	private String methodName;

	public DelegateEnhancementHint(Class<?> type, String methodName, Class<?> parameterType) {
		this.type = type;
		this.methodName = methodName;
		this.parameterType = parameterType;
	}

	public Class<?> getType() {
		return type;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class<?> getParameterType() {
		return parameterType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof DelegateEnhancementHint)) {
			return false;
		}
		DelegateEnhancementHint other = (DelegateEnhancementHint) obj;
		return EqualsUtil.equals(other.getType(), getType())
				&& EqualsUtil.equals(other.getMethodName(), getMethodName())
				&& EqualsUtil.equals(other.getParameterType(), getParameterType());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ getType().hashCode() ^ getMethodName().hashCode()
				^ getParameterType().hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType) {
		if (DelegateEnhancementHint.class.isAssignableFrom(includedContextType)) {
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance) {
		return getType().getName() + "$Delegate$" + getMethodName();
	}
}
