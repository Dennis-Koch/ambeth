package com.koch.ambeth.security;

import java.util.Objects;

/*-
 * #%L
 * jambeth-security
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

import com.koch.ambeth.service.model.ISecurityScope;

public class StringSecurityScope implements ISecurityScope {
	public static final String DEFAULT_SCOPE_NAME = "defaultScope";

	public static final ISecurityScope DEFAULT_SCOPE = new StringSecurityScope(DEFAULT_SCOPE_NAME);

	protected String name;

	public StringSecurityScope() {
	}

	public StringSecurityScope(String name) {
		setName(name);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (this.name != null) {
			throw new IllegalStateException("Name is only allowed to be specified once");
		}
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof StringSecurityScope)) {
			return false;
		}
		StringSecurityScope other = (StringSecurityScope) obj;
		return Objects.equals(getName(), other.getName());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ getName().hashCode();
	}
}
