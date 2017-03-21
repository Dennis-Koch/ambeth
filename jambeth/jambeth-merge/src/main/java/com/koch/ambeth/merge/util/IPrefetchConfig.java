package com.koch.ambeth.merge.util;

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

public interface IPrefetchConfig {
	/**
	 * Returns a stub looking like the requested entity type. All relations on this stub can be
	 * accessed (to-one/to-many) in a cascaded manner. The traversal is internally tracked as a
	 * prefetch path configuration. For to-many relations there is always exactly one entity stub in
	 * the collection available for valid traversal.<br>
	 * For example:<br>
	 * <code>
	 * IPrefetchConfig pc = prefetchHelper.createPrefetch();<br>
	 * pc.plan(MyEntity.class).getMyToOne().getMyToMany().get(0).getFunnyRel();<br>
	 * IPrefetchHandle ph = pc.build();<br>
	 * </code><br>
	 * Does exactly the same as:<br>
	 * <code>
	 * IPrefetchConfig pc = prefetchHelper.createPrefetch();<br>
	 * pc.add(MyEntity.class, "MyToOne.MyToMany.FunnyRel");<br>
	 * IPrefetchHandle ph = pc.build();<br>
	 * </code><br>
	 * The major difference is that the stub traversal is supported via code completion of our chosen
	 * IDE and eagerly compiled to detect typos immediately. The latter one could be loaded e.g. from
	 * a configuration file or generic string concatenation with ease.
	 *
	 * @param entityType The requested entity type to create a stub of
	 * @return A stub of the requested entity type
	 */
	<T> T plan(Class<T> entityType);

	IPrefetchConfig add(Class<?> entityType, String propertyPath);

	IPrefetchConfig add(Class<?> entityType, String... propertyPaths);

	IPrefetchHandle build();
}
