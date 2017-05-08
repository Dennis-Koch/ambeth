package com.koch.ambeth.cache.cacheretriever;

/*-
 * #%L
 * jambeth-test
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

import java.util.Set;

import com.koch.ambeth.model.AbstractEntity;

public class LocalEntity extends AbstractEntity {
	protected String name;

	protected int value;

	protected ExternalEntity2 parent;

	protected Set<ExternalEntity> externals;

	protected ExternalEntity sibling;

	// protected Set<ExternalEntity2> externals2;

	protected LocalEntity() {
		// Intended blank
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public ExternalEntity2 getParent() {
		return parent;
	}

	public void setParent(ExternalEntity2 parent) {
		this.parent = parent;
	}

	public Set<ExternalEntity> getExternals() {
		return externals;
	}

	public void setExternals(Set<ExternalEntity> externals) {
		this.externals = externals;
	}

	public ExternalEntity getSibling() {
		return sibling;
	}

	public void setSibling(ExternalEntity sibling) {
		this.sibling = sibling;
	}

	// public Set<ExternalEntity2> getExternals2()
	// {
	// return externals2;
	// }
	//
	// public void setExternals2(Set<ExternalEntity2> externals2)
	// {
	// this.externals2 = externals2;
	// }
}
