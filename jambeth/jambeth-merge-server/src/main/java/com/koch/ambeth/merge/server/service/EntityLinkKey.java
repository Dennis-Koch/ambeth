package com.koch.ambeth.merge.server.service;

/*-
 * #%L
 * jambeth-merge-server
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

import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.persistence.api.IDirectedLink;

public class EntityLinkKey {
	protected final RootCacheValue entity;

	protected final IDirectedLink link;

	public EntityLinkKey(RootCacheValue entity, IDirectedLink link) {
		this.entity = entity;
		this.link = link;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof EntityLinkKey)) {
			return false;
		}
		EntityLinkKey other = (EntityLinkKey) obj;
		return entity.equals(other.entity) && link.equals(other.link);
	}

	@Override
	public int hashCode() {
		return entity.hashCode() ^ link.hashCode();
	}

	@Override
	public String toString() {
		return entity + " - " + link.getMetaData().getName();
	}
}
