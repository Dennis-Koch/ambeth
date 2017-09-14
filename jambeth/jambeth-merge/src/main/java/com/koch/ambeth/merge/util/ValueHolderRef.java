package com.koch.ambeth.merge.util;

import java.util.Objects;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;

public class ValueHolderRef {
	protected IObjRef objRef;

	protected RelationMember member;

	protected int relationIndex;

	public ValueHolderRef(IObjRef objRef, RelationMember member, int relationIndex) {
		this.objRef = objRef;
		this.member = member;
		this.relationIndex = relationIndex;
	}

	public IObjRef getObjRef() {
		return objRef;
	}

	public RelationMember getMember() {
		return member;
	}

	public int getRelationIndex() {
		return relationIndex;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ValueHolderRef)) {
			return false;
		}
		ValueHolderRef other = (ValueHolderRef) obj;
		return Objects.equals(getObjRef(), other.getObjRef())
				&& getRelationIndex() == other.getRelationIndex();
	}

	@Override
	public int hashCode() {
		return getObjRef().hashCode() ^ getRelationIndex();
	}
}
