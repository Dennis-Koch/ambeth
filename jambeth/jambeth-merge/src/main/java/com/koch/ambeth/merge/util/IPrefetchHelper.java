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

import java.util.List;

import com.koch.ambeth.util.collections.IList;

/**
 * Works as a factory for {@link IPrefetchConfig} instances
 */
public interface IPrefetchHelper {
	/**
	 * Factory method for {@link IPrefetchConfig} instances. After configuring the
	 * <code>IPrefetchConfig</code> a call to <code>.build()</code> creates the real
	 * {@link IPrefetchHandle} to be used for prefetching graphs in an entity model. The complexity of
	 * the batch algorithm to evaluate the needed fetching iterations is defined by the depth of the
	 * entity graph (not the width of the graph or the amount of entities prefetched). With this
	 * approach it is even possible to initialize a complete hierarchy of self-relational entities
	 * (e.g. a UserRole/UserRole tree) very efficiently (again: scaling with the hierarchy depth, not
	 * the amount of UserRoles)<br>
	 * <br>
	 * Usage:<br>
	 * <code>
	 * List<?> myEntities = ...<br>
	 * IPrefetchConfig prefetchConfig = prefetchHelper.createPrefetch();<br>
	 * prefetchConfig.plan(MyEntity.class).getMyRelations().get(0).getMyTransitiveRelation();<br>
	 * prefetchConfig.plan(MyEntity.class).getMyOtherRelation();<br>
	 * prefetchConfig.plan(MyOtherEntity.class).getMyEntity();<br>
	 * IPrefetchHandle prefetchHandle = prefetchConfig.build();<br>
	 * prefetchHandle.prefetch(myEntities);<br>
	 * </code>
	 *
	 * @return An empty IPrefetchConfig. Needs to be configured before a call to
	 *         {@link IPrefetchConfig#build()} finishes the configuration and creates a handle to work
	 *         with.
	 */
	IPrefetchConfig createPrefetch();

	/**
	 * Allows to prefetch pointers to relations of entities in a very fine-grained manner - but still
	 * allowing the lowest amount of batched fetch operations possible (and therefore with the least
	 * possible amount of potential remote or database calls). It expects to work not directly with
	 * entities (because it would not know which relation of those entities to prefetch) but to work
	 * with composite handles describing a relation of an entity
	 * (={@link com.koch.ambeth.cache.util.IndirectValueHolderRef}) or an entity instance
	 * (={@link DirectValueHolderRef}).<br>
	 * <br>
	 * A special benefit of these composite handles is that it even allows you to define to only
	 * prefetch the object references of a relation but not the real initialized relations. This could
	 * be helpful if you only want to do something like a "count" on the relation or only want to know
	 * the entity identifiers on the relation, not the complete payload of the related entities.<br>
	 * <br>
	 * Usage:<br>
	 * <code>
	 * IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entity1.getClass());<br>
	 * RelationMember myRelation = (RelationMember) metaData.getMemberByName("MyFunnyRelation");<br>
	 * IObjRefContainer myEntity1 = (IObjRefContainer)entity1;<br>
	 * IObjRefContainer myEntity2 = (IObjRefContainer)entity2;<br>
	 * List<?> myPrefetchRelations = Arrays.asList(new DirectValueHolderRef(myEntity1, myRelation, true), new DirectValueHolderRef(myEntity2, myRelation, true));<br>
	 * prefetchHelper.prefetch(myPrefetchRelations);<br>
	 * IObjRef[] relationPointersOfEntity1 = myEntity1.get__ObjRefs(metaData.getIndexByRelation(myRelation));<br>
	 * IObjRef[] relationPointersOfEntity2 = myEntity2.get__ObjRefs(metaData.getIndexByRelation(myRelation));<br>
	 * </code> *
	 *
	 * @param objects A collection or array of instances of either {@link DirectValueHolderRef} or
	 *        {@link com.koch.ambeth.cache.util.IndirectValueHolderRef}
	 * @return The hard reference to all resolved entity instances of requested relations. It is
	 *         reasonable to store this result on the stack (without working with the stack variable)
	 *         in cases where the associated cache instances are configured to hold entity instances
	 *         in a weakly manner. In those cases it may happen that a prefetch request increases the
	 *         amount of cached entities temporarily above the LRU limit (least recently used
	 *         algorithm). Therefore it is necessary to ensure that the initialized result
	 *         continuously gets a hold in the cache as long as needed for the process on the stack.
	 *         So this hard reference compensates the cases where the LRU cache would consider the
	 *         resolved entity instances as "releasable". If the corresponding caches are generally
	 *         configured to hold hard references to the entity instances themselves the LRU cache is
	 *         effectively disabled and the returned {@link IPrefetchState} has no effective use.
	 */
	IPrefetchState prefetch(Object objects);

	/**
	 * Convenience method for specific usecases of the {@link #createPrefetch()} approach. With this
	 * it is possible to prefetch a single deep graph of a set of entity relations and to aggregate
	 * all distinct entities of those initialized graph leafs as a result. This also works for
	 * not-yet-persisted entities referenced in the graph.<br>
	 * <br>
	 * Usage:<br>
	 * <code>
	 * List&lt;MyOtherRelation&gt; allMyOtherRelations = prefetchHelper.extractTargetEntities(myEntities, "Relations.MyRelation.MyOtherRelation", MyEntity.class);
	 * </code>
	 *
	 * @param sourceEntities The entity instances each working as the graph root for the batched
	 *        prefetch operation
	 * @param sourceToTargetEntityPropertyPath The transitive traversal path to be initialized based
	 *        on each given graph root in 'sourceEntities'
	 * @param sourceEntityType The common entity type of the set of entity instances in
	 *        'sourceEntities'
	 * @return The aggregated list of distinct entity instances resolved as the leaf of the graph
	 *         traversal defined by 'sourceToTargetEntityPropertyPath'
	 */
	<T, S> IList<T> extractTargetEntities(List<S> sourceEntities,
			String sourceToTargetEntityPropertyPath, Class<S> sourceEntityType);
}
