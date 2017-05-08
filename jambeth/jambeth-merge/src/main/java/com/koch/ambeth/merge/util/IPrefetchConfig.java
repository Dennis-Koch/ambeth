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

/**
 * Temporary handle to be used to configure a prefetch plan. When the configuration is completed a
 * call to {@link #build()} is needed to create the real prefetch handle of type
 * {@link IPrefetchHandle}. This instance can be created by calling
 * {@link IPrefetchHelper#createPrefetch()}.
 */
public interface IPrefetchConfig {
	/**
	 * Returns a stub looking like the requested entity type. All relations on this stub can be
	 * accessed (to-one/to-many) in a cascaded manner. The traversal is internally tracked as a
	 * prefetch path configuration. For to-many relations there is always exactly one entity stub in
	 * the collection available for valid traversal.<br>
	 * <br>
	 * Example:<br>
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
	 * IDE and eagerly compiled to detect typos immediately. The latter one instead could be loaded
	 * e.g. from a configuration file or generic string concatenation with ease.
	 *
	 * @param entityType The requested entity type to create a stub of it
	 * @return A stub of the requested entity type
	 */
	<T> T plan(Class<T> entityType);

	/**
	 * Configures to prefetch the given graph traversal having its base on entity instances of the
	 * given type.<br>
	 * <br>
	 * Usage:<br>
	 * <code>
	 * IPrefetchHandle ph = prefetchHelper.createPrefetch().add(MyEntity.class, "MyToOne.MyToMany.FunnyRel").build();<br>
	 * </code>
	 *
	 * @param entityType The entity type where the graph traversal has its root
	 * @param propertyPath The graph traversal where each relational step is separated by a dot '.'
	 * @return
	 */
	IPrefetchConfig add(Class<?> entityType, String propertyPath);

	/**
	 * Configures to prefetch the given graph traversals having its base on entity instances of the
	 * given type. This is effectively just a convenience method of {@link #add(Class, String)} if you
	 * want to prefetch several partially or completely different traversals but having the same
	 * entity type as their root.<br>
	 * <br>
	 * Usage:<br>
	 * <code> IPrefetchHandle ph =
	 * prefetchHelper.createPrefetch().add(MyEntity.class, "MyToOne", "MyOtherToOne", "MyToMany.FunnyRel").build();<br>
	 * </code>
	 *
	 * @param entityType
	 * @param propertyPaths
	 * @return
	 */
	IPrefetchConfig add(Class<?> entityType, String... propertyPaths);

	/**
	 * Finalizes the configuration and creates the runtime handle for doing prefetches on given entity
	 * sets. The created handle considers all previous calls to {@link #add(Class, String)},
	 * {@link #add(Class, String...)} and {@link IPrefetchConfig#plan(Class)}. The
	 * {@link IPrefetchHandle} is thread-safe and can be pre-build in an application init phase and
	 * stored e.g. on an instance field for fast concurrent later access.<br>
	 * <br>
	 * Usage:<br>
	 * <code>
	 * IPrefetchHelper prefetchHelper;<br>
	 * IPrefetchHandle myPrefetch;<br>
	 * public void init() {<br>
	 *   myPrefetch = prefetchHelper.createPrefetch().add(MyEntity.class, "MyToOne").build();<br>
	 * }<br><br>
	 *
	 * public void doWork(List&lt;MyEntity&gt; entities) {<br>
	 *    myPrefetch.prefetch(entities);<br>
	 *    for (MyEntity myEntity : entities) {<br>
	 *      ...<br>
	 *      myEntity.getMyToOne().getValue();
	 *      ...<br>
	 *    }<br>
	 * }<br>
	 * </code>
	 *
	 * @return
	 */
	IPrefetchHandle build();
}
