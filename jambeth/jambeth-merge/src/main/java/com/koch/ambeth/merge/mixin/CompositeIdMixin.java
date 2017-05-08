package com.koch.ambeth.merge.mixin;

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

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

public class CompositeIdMixin {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public boolean equalsCompositeId(ITypeInfoItem[] members, Object left, Object right) {
		if (left == null || right == null) {
			return false;
		}
		if (left == right) {
			return true;
		}
		if (!left.getClass().equals(right.getClass())) {
			return false;
		}
		for (ITypeInfoItem member : members) {
			Object leftValue = member.getValue(left, false);
			Object rightValue = member.getValue(right, false);
			if (leftValue == null || rightValue == null) {
				return false;
			}
			if (!leftValue.equals(rightValue)) {
				return false;
			}
		}
		return true;
	}

	public int hashCodeCompositeId(ITypeInfoItem[] members, Object compositeId) {
		int hash = compositeId.getClass().hashCode();
		for (ITypeInfoItem member : members) {
			Object value = member.getValue(compositeId, false);
			if (value != null) {
				hash ^= value.hashCode();
			}
		}
		return hash;
	}

	public String toStringCompositeId(ITypeInfoItem[] members, Object compositeId) {
		StringBuilder sb = new StringBuilder();
		toStringSbCompositeId(members, compositeId, sb);
		return sb.toString();
	}

	public void toStringSbCompositeId(ITypeInfoItem[] members, Object compositeId, StringBuilder sb) {
		// order does matter here
		for (int a = 0, size = members.length; a < size; a++) {
			Object value = members[a].getValue(compositeId);
			if (a > 0) {
				sb.append('#');
			}
			if (value != null) {
				StringBuilderUtil.appendPrintable(sb, value);
			}
			else {
				sb.append("<null>");
			}
		}
	}
}
