package com.koch.ambeth.cache.util;

/*-
 * #%L
 * jambeth-cache
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

public class PrefetchPath {
	public final int memberIndex;

	public final String memberName;

	public final Class<?> memberType;

	public final PrefetchPath[] children;

	public final Class<?>[] memberTypesOnDescendants;

	public PrefetchPath(Class<?> memberType, int memberIndex, String memberName,
			PrefetchPath[] children, Class<?>[] memberTypesOnDescendants) {
		this.memberIndex = memberIndex;
		this.memberName = memberName;
		this.memberType = memberType;
		this.children = children;
		this.memberTypesOnDescendants = memberTypesOnDescendants;
	}

	@Override
	public String toString() {
		return memberName;
	}
}
