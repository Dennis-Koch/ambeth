package com.koch.ambeth.util.collections;

/*-
 * #%L
 * jambeth-util
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

import java.util.Map.Entry;

/**
 * Wird von verschiedenen Map-Implementierungen als Entry fuer die Key-Value Mappings benoetigt
 *
 * @author kochd
 *
 * @param <K>
 *          Der Typ des Keys
 * @param <V>
 *          Der Typ des Values
 */
public interface IMapEntry<K, V> extends Entry<K, V> {
	int getHash();

	IMapEntry<K, V> getNextEntry();

	@Override
	V setValue(V value);

	boolean isValid();
}
