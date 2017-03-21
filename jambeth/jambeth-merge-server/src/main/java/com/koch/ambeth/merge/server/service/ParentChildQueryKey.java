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

import java.util.Arrays;

import com.koch.ambeth.util.IPrintable;

public class ParentChildQueryKey implements IPrintable {
	protected final Class<?> selectedEntityType;

	protected final String selectingMemberName;

	protected final String[] childMemberNames;

	public ParentChildQueryKey(Class<?> selectedEntityType, String selectingMemberName,
			String[] childMemberNames) {
		this.selectedEntityType = selectedEntityType;
		this.selectingMemberName = selectingMemberName;
		this.childMemberNames = childMemberNames;
	}

	@Override
	public int hashCode() {
		return selectedEntityType.hashCode() ^ selectingMemberName.hashCode()
				^ Arrays.hashCode(childMemberNames);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ParentChildQueryKey)) {
			return false;
		}
		ParentChildQueryKey other = (ParentChildQueryKey) obj;
		return selectedEntityType.equals(other.selectedEntityType)
				&& selectingMemberName.equals(other.selectingMemberName)
				&& Arrays.equals(childMemberNames, other.childMemberNames);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(getClass().getSimpleName()).append(": SelectedEntityType=")
				.append(selectedEntityType.getName()).append("\r\tSelectingMemberName=")
				.append(selectingMemberName);
		sb.append("\r\tChildMemberNames=").append(Arrays.toString(childMemberNames));
	}

}
