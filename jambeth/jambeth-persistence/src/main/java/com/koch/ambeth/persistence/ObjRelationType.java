package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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
import com.koch.ambeth.util.IPrintable;

public class ObjRelationType implements IPrintable {
	protected final Class<?> entityType;

	protected final byte idIndex;

	protected final String memberName;

	public ObjRelationType(Class<?> entityType, byte idIndex, String memberName) {
		this.entityType = entityType;
		this.idIndex = idIndex;
		this.memberName = memberName;
	}

	public Class<?> getEntityType() {
		return entityType;
	}

	public byte getIdIndex() {
		return idIndex;
	}

	public String getMemberName() {
		return memberName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ObjRelationType)) {
			return false;
		}
		ObjRelationType other = (ObjRelationType) obj;
		return EqualsUtil.equals(getEntityType(), other.getEntityType())
				&& getIdIndex() == other.getIdIndex()
				&& EqualsUtil.equals(getMemberName(), other.getMemberName());
	}

	@Override
	public int hashCode() {
		return getEntityType().hashCode() ^ getIdIndex() ^ getMemberName().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	private String getClassName() {
		return "ObjRel";
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(getClassName()).append(" idIndex=").append(idIndex).append(" type=")
				.append(entityType.getName()).append(" property=").append(memberName);
	}
}
