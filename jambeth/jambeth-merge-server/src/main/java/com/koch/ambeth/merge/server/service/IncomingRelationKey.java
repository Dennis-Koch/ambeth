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

import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.ITable;

public class IncomingRelationKey {
	protected final byte idIndex;

	protected final ITable table;

	protected final IDirectedLink link;

	public IncomingRelationKey(byte idIndex, ITable table, IDirectedLink link) {
		this.idIndex = idIndex;
		this.table = table;
		this.link = link;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof IncomingRelationKey)) {
			return false;
		}
		IncomingRelationKey other = (IncomingRelationKey) obj;
		return idIndex == other.idIndex && table.equals(other.table) && link.equals(other.link);
	}

	@Override
	public int hashCode() {
		return idIndex ^ table.hashCode() ^ link.hashCode();
	}

	@Override
	public String toString() {
		return idIndex + " " + table.getMetaData().getName() + " - " + link.getMetaData().getName();
	}
}
