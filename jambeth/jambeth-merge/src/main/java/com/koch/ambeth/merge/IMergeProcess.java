package com.koch.ambeth.merge;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.service.IMergeService;

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
 * The single generic component to do all transitions in the entity model. This means handling all
 * newly instantiated entities, updated entities, deleted entities as well as all relational changes
 * between entities. The Merge Process internally calculates a single diff for the transition of all
 * referenced entities from their old state (=committed state) to the new state (=given state as
 * arguments). The diff is an instance of {@link ICUDResult} and will be passed to an
 * {@link IMergeService}. In most client deployment scenarios this Merge Service would be a stub for
 * a remote endpoint. In server deployment scenarios the Merge Service would split the transition up
 * into the corresponding connected persistent repositories (e.g. JDBC database, file system, 3rd
 * party web service calls, in memory persistence, ...)
 */
public interface IMergeProcess {
	IMergeProcessStarted begin();

	/**
	 * Most simplictic overload of the Merge Process methods. It just expects any structure referring
	 * to entities. Structure means here: A single entity instance, a collection of entities (anything
	 * derived from {@link Iterable} or an array). You can also indirectly pass entities you want to
	 * delete with this method by using the {@link com.koch.ambeth.util.model.IDataObject} cast.<br>
	 * <br>
	 * Usage example:<br>
	 * <br>
	 * <code>
	 * MyEntity myEntity = ...;<br>
	 * ((IDataObject)myEntity).setToBeDeleted(true);<br>
	 * mergeProcess.process(myEntity);
	 * </code><br>
	 * <br>
	 * For more fine-grained control of the merge process please use the fluent API provided by
	 * {@link #begin()}.
	 *
	 *
	 * @param objectsToMerge The entities you want to merge - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op.
	 */
	void process(Object objectsToMerge);

	/**
	 * Most simplictic overload of the Merge Process methods. It just expects any structure referring
	 * to entities. Structure means here: A single entity instance, a collection of entities (anything
	 * derived from {@link Iterable} or an array). You can also indirectly pass entities you want to
	 * delete with this method by using the {@link com.koch.ambeth.util.model.IDataObject} cast.<br>
	 * <br>
	 * Usage example:<br>
	 * <br>
	 * <code>
	 * MyEntity myEntity = ...;<br>
	 * ((IDataObject)myEntity).setToBeDeleted(true);<br>
	 * mergeProcess.process(myEntity);
	 * </code><br>
	 * <br>
	 * For more fine-grained control of the merge process please use the fluent API provided by
	 * {@link #begin()}.
	 *
	 * @param objectsToMerge1 The entities you want to merge - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op.
	 * @param objectsToMerge2 The entities you want to merge - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op.
	 */
	@SuppressWarnings("unchecked")
	<T> void process(T objectsToMerge1, T... objectsToMerge2);
}
