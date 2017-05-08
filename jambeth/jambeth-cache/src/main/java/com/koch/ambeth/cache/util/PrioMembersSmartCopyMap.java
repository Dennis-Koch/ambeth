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

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.IMapEntry;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.SmartCopyMap;

public class PrioMembersSmartCopyMap
		extends SmartCopyMap<PrioMembersKey, IdentityLinkedSet<Member>> {
	public PrioMembersSmartCopyMap() {
		super(0.5f);
	}

	@Override
	protected boolean equalKeys(PrioMembersKey key,
			IMapEntry<PrioMembersKey, IdentityLinkedSet<Member>> entry) {
		PrioMembersKey other = entry.getKey();
		if (key == other) {
			return true;
		}
		IdentityLinkedSet<Member> key1 = key.getKey1();
		IdentityLinkedSet<Member> otherKey1 = other.getKey1();
		if (key1.size() != otherKey1.size()) {
			return false;
		}
		for (Member item : key1) {
			if (!otherKey1.contains(item)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected int extractHash(PrioMembersKey key) {
		IdentityLinkedSet<Member> key1 = key.getKey1();
		int hash = 91 ^ key1.size();
		for (Member item : key1) {
			hash ^= item.hashCode();
		}
		return hash;
	}
}
