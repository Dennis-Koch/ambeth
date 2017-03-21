package com.koch.ambeth.merge.cache;

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

import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public abstract class AbstractCacheValue implements IPrintable {
	public abstract Object getId();

	public abstract void setId(Object id);

	public abstract Object getVersion();

	public abstract void setVersion(Object version);

	public abstract Class<?> getEntityType();

	public abstract Object getPrimitive(int primitiveIndex);

	public abstract Object[] getPrimitives();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("EntityType=").append(getEntityType().getName()).append(" Id='");
		StringBuilderUtil.appendPrintable(sb, getId());
		sb.append("' Version='").append(getVersion()).append('\'');
	}
}
