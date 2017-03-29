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
 * Allows to execute a previously configured prefetch plan on a given set of entities used as a
 * graph root for traversal. The prefetch algorithm is optimized to reduce the amount of roundtrips
 * to a database or remote source to fulfull the necessary fetches needed to initialize all
 * relations touched by the prefetch plan. It scales with the depth of the graph traversal, not the
 * width (=general amount of entities at any given step during the traversal).
 */
public interface IPrefetchHandle {

	/**
	 * Execute the prefetch plan to initialize all touched relations in the graph traversal
	 *
	 * @param objects A handle to the entities to initialize. It may be a single entity, a collection
	 *        or an array of entities in any arbitrary bag hierarchy. So collection of arrays of
	 *        collections of entities would work.
	 * @return
	 */
	IPrefetchState prefetch(Object objects);

	/**
	 * Execute the prefetch plan to initialize all touched relations in the graph traversal. This is
	 * just a convenience method for {@link #prefetch(Object)} and a call to this like
	 * '.prefetch(entity1, entity2)' is essentially equivalent to the call
	 * '.prefetch(Arrays.asList(entity1, entity2))'.
	 *
	 * @param objects
	 * @return
	 */
	IPrefetchState prefetch(Object... objects);

	/**
	 * Allows to union two or more distinct instances of a {@link IPrefetchHandle} to create a new,
	 * independent third instance describing an aggregated prefetch plan based on the given base
	 * plans.
	 *
	 * @param otherPrefetchHandles One ore more prefetch plans to aggregate with this prefetch plan
	 * @return The aggregated prefetch plan.
	 */
	IPrefetchHandle union(IPrefetchHandle... otherPrefetchHandles);
}
