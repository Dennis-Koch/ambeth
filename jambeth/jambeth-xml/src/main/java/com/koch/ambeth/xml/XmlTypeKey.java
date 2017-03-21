package com.koch.ambeth.xml;

/*-
 * #%L
 * jambeth-xml
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

public class XmlTypeKey implements IPrintable, IXmlTypeKey {
	protected final String name;

	protected final String namespace;

	public XmlTypeKey(String name, String namespace) {
		this.name = name;
		this.namespace = namespace;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public int hashCode() {
		if (namespace == null) {
			return name.hashCode();
		}
		return name.hashCode() ^ namespace.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XmlTypeKey)) {
			return false;
		}
		IXmlTypeKey other = (IXmlTypeKey) obj;
		return EqualsUtil.equals(name, other.getName())
				&& EqualsUtil.equals(namespace, other.getNamespace());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("XmlTypeKey: ").append(name);
		if (namespace != null) {
			sb.append(" ").append(namespace);
		}
	}
}
