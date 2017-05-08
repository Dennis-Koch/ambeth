package com.koch.ambeth.ioc.typeinfo;

/*-
 * #%L
 * jambeth-ioc
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

import java.util.Collections;
import java.util.Comparator;

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public class PropertyInfoEntry {
	protected final IMap<String, IPropertyInfo> map;

	protected final IPropertyInfo[] properties;

	public PropertyInfoEntry(IMap<String, IPropertyInfo> map) {
		this.map = map;
		ArrayList<IPropertyInfo> pis = new ArrayList<>(map.toArray(IPropertyInfo.class));
		Collections.sort(pis, new Comparator<IPropertyInfo>() {
			@Override
			public int compare(IPropertyInfo o1, IPropertyInfo o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		properties = pis.toArray(IPropertyInfo.class);
	}
}
