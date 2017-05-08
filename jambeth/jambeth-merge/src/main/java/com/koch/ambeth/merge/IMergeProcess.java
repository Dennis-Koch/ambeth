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
	/**
	 * Most simplictic overload of the Merge Process methods. It just expects any structure referring
	 * to entities. Structure means here: A single entity instance, a collection of entities (anything
	 * derived from {@link Iterable} or an array). You can also indirectly pass entities you want to
	 * delete with this method by using the {@link com.koch.ambeth.util.model.IDataObject} cast.<br>
	 * <br>
	 * Usage example:<br>
	 * <code>
	 * MyEntity myEntity = ...; ((IDataObject)myEntity) pass objectsToDelete" ((IDataObject)
	 * myEntity).setToBeDeleted(true); mergeProcess.process(myEntity);
	 * </code>
	 *
	 * @param objectsToMerge The entities you want to merge - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op.
	 */
	void process(Object objectsToMerge);

	/**
	 * In addition to {@ #process(Object)} it allows to explicitly pass entities as a 'toBeDeleted'
	 * parameter. Note that in cases where you have just a set of entities and do not know (any more)
	 * whether they have to receive a create/update or a delete you can just use {@ #process(Object)}
	 * if the corresponding entities have specified their to-be-deleted flag correctly. sIt just
	 * expects any structure referring to entities. Structure means here: A single entity instance, a
	 * collection of entities (anything derived from {@link Iterable} or an array). You can also
	 * indirectly pass entities you want to delete with this method by using the
	 * {@link com.koch.ambeth.util.model.IDataObject} cast.<br>
	 *
	 * @param objectsToMerge The entities you want to merge - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op.
	 * @param objectsToDelete The entities you want to delete - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op. Note
	 *        that technically it may be still possible that entities passed to 'objectsToMerge' might
	 *        have an explicit to-be-deleted flag as well. If an entity is passed to both parameters
	 *        it will get deleted (and its transition dropped).
	 */
	void process(Object objectsToMerge, Object objectsToDelete);

	/**
	 * In addition to {@ #process(Object, Object)} this overload allows to pass two additional
	 * optional arguments to the Merge Process.
	 *
	 * @param objectsToMerge The entities you want to merge - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op.
	 * @param objectsToDelete The entities you want to delete - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op. Note
	 *        that technically it may be still possible that entities passed to 'objectsToMerge' might
	 *        have an explicit to-be-deleted flag as well. If an entity is passed to both parameters
	 *        it will get deleted (and its transition dropped).
	 * @param proceedHook A custom callback called after the initial transition-diff is created as an
	 *        instance of {@link ICUDResult}. The callback may decide to early-break the Merge Process
	 *        for any reason before it gets processed in more detail by the {@link IMergeService}.
	 * @param mergeFinishedCallback A custom callback called after the Merge Process finished its
	 *        operation. This is specifically helpful in client deployment scenarios with an active UI
	 *        thread: In those cases the Merge Process is an asynchronous operation (to not block the
	 *        UI thread on-the-run). Then this callback can be used to receive the "finish event" of
	 *        the asynchronous task.
	 */
	void process(Object objectsToMerge, Object objectsToDelete, ProceedWithMergeHook proceedHook,
			MergeFinishedCallback mergeFinishedCallback);

	/**
	 * In addition to {@ #process(Object, Object, ProceedWithMergeHook, MergeFinishedCallback)} this
	 * overload allows to pass two additional optional arguments to the Merge Process.
	 *
	 * @param objectsToMerge The entities you want to merge - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op.
	 * @param objectsToDelete The entities you want to delete - including any transitive relationship.
	 *        May also be null or an empty collection/array which would just result in a no-op. Note
	 *        that technically it may be still possible that entities passed to 'objectsToMerge' might
	 *        have an explicit to-be-deleted flag as well. If an entity is passed to both parameters
	 *        it will get deleted (and its transition dropped).
	 * @param proceedHook An optional custom callback called after the initial transition-diff is
	 *        created as an instance of {@link ICUDResult}. The callback may decide to early-break the
	 *        Merge Process for any reason before it gets processed in more detail by the
	 *        {@link IMergeService}.
	 * @param mergeFinishedCallback An optional custom callback called after the Merge Process
	 *        finished its operation. This is specifically helpful in client deployment scenarios with
	 *        an active UI thread: In those cases the Merge Process is an asynchronous operation (to
	 *        not block the UI thread on-the-run). Then this callback can be used to receive the
	 *        "finish event" of the asynchronous task.
	 * @param addNewEntitiesToCache Gives the Merge Process a hint whether newly created entity
	 *        instances should also be added to the current 1st level cache. The default value is
	 *        "true". This flag can be set to "false" in cases where you do large amounts of "fire and
	 *        forget" entity creations and want save the efforts to treat them in the cache without
	 *        any real need to further access exactly the given instances. Note that even with "false"
	 *        ordinary cached behavior still applies when the entity is requested later by any thread.
	 *        Regarding this flag being set to "true" (which again is also the default) be also aware
	 *        about consequences of using a THREAD_LOCAL 1st level cache in deployment scenarios where
	 *        the Ambeth UI thread behavior is activated at the same time. If in those cases the
	 *        initial call to the Merge Process is done from within the UI thread the entities get put
	 *        to a thread-local cache instance owned by a worker thread (i.e. not owned by the
	 *        UI-thread) which might not be what you want. This is another reason why a SINGLETON 1st
	 *        level cache is recommended when using the Ambeth stack in deployments where a
	 *        "UI-thread-aware behavior" (=not blocking the UI at any time) is also expected from
	 *        Ambeth
	 */
	void process(Object objectsToMerge, Object objectsToDelete, ProceedWithMergeHook proceedHook,
			MergeFinishedCallback mergeFinishedCallback, boolean addNewEntitiesToCache);
}
