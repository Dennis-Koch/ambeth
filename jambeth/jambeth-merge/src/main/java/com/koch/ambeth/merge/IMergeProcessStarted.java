package com.koch.ambeth.merge;

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.service.IMergeService;

public interface IMergeProcessStarted {
	/**
	 * It just expects any structure referring to entities that need to be merged. Structure means
	 * here: A single entity instance, a collection of entities (anything derived from
	 * {@link Iterable} or an array). You can also indirectly pass entities you want to delete with
	 * this method by using the {@link com.koch.ambeth.util.model.IDataObject} cast.<br>
	 * <br>
	 * Usage example:<br>
	 * <code>
	 * MyEntity myEntity = ...; ((IDataObject)myEntity) pass objectsToDelete" ((IDataObject)
	 * myEntity).setToBeDeleted(true); mergeProcess.process(myEntity);
	 * </code>
	 *
	 * @param objectsToMerge The entities you want to merge - by default this means implicitly a deep
	 *        merge as long as on each traversal step the dirty state of the given entities is
	 *        verified). May also be null or an empty collection/array which would just result in a
	 *        no-op.
	 */
	IMergeProcessContent merge(Object objectsToMerge);

	/**
	 * It just expects any structure referring to entities that need to be merged. Structure means
	 * here: A single entity instance, a collection of entities (anything derived from
	 * {@link Iterable} or an array). You can also indirectly pass entities you want to delete with
	 * this method by using the {@link com.koch.ambeth.util.model.IDataObject} cast.<br>
	 * <br>
	 * Usage example:<br>
	 * <code>
	 * MyEntity myEntity = ...; ((IDataObject)myEntity) pass objectsToDelete" ((IDataObject)
	 * myEntity).setToBeDeleted(true); mergeProcess.begin().merge(myEntity).finish();
	 * </code>
	 *
	 * @param objectsToMerge1 The entities you want to merge - including any transitive relationship
	 *        (= deep merge as long as on each traversal step the dirty state of the given entites is
	 *        verified). May also be null or an empty collection/array which would just result in a
	 *        no-op.
	 * @param objectsToMerge2 The entities you want to merge - including any transitive relationship
	 *        (= deep merge as long as on each traversal step the dirty state of the given entites is
	 *        verified). May also be null or an empty collection/array which would just result in a
	 *        no-op.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	<T> IMergeProcessContent merge(T objectsToMerge1, T... objectsToMerge2);

	/**
	 * Allows to mark any structure referring to entities as to-be-deleted. After the merge process
	 * has finished successfully the given entity instance handles have an erased primary identifier
	 * and an erased version and have been detached from their previous 1st level cache. In theory the
	 * same instance of an entity could be used to "re-create" another persisted incarnation of the
	 * same data by directly passing it again as "to-be-merged" into the next merge process.
	 *
	 * @param objectsToDelete The explicit entities you want to delete - without traversing any of
	 *        their relationship (= shallow merge). May also be null or an empty collection/array
	 *        which would just result in a no-op.
	 * @return The fluent-API handle for the current merge process instance on the stack. Call
	 *         {@link #finish()} to execute the configured merge
	 */
	IMergeProcessContent delete(Object objectsToDelete);

	/**
	 * Allows to mark any structure referring to entities as to-be-deleted. After the merge process
	 * has finished successfully the given entity instance handles have an erased primary identifier
	 * and an erased version and have been detached from their previous 1st level cache. In theory the
	 * same instance of an entity could be used to "re-create" another persisted incarnation of the
	 * same data by directly passing it again as "to-be-merged" into the next merge process.
	 *
	 * @param objectsToDelete1 The explicit entities you want to delete - without traversing any of
	 *        their relationship (= shallow merge). May also be null or an empty collection/array
	 *        which would just result in a no-op.
	 * @param objectsToDelete2 The explicit entities you want to delete - without traversing any of
	 *        their relationship (= shallow merge). May also be null or an empty collection/array
	 *        which would just result in a no-op.
	 * @return The fluent-API handle for the current merge process instance on the stack. Call
	 *         {@link #finish()} to execute the configured merge
	 */
	@SuppressWarnings("unchecked")
	<T> IMergeProcessContent delete(T objectsToDelete1, T... objectsToDelete2);

	/**
	 * An optional custom callback called after the initial transition-diff is created as an instance
	 * of {@link ICUDResult}. The callback may decide to early-break the Merge Process for any reason
	 * before it gets processed in more detail by the {@link IMergeService}.
	 *
	 * @param hook The invoked callback after a non-empty diff has been resolved considering all given
	 *        to-be-merged and to-be-deleted entities and comparing them to their "old" committed
	 *        state. This includes simple property changes as well as any relational change between
	 *        those entities.
	 * @return The fluent-API handle for the current merge process instance on the stack. Call
	 *         {@link #finish()} to execute the configured merge
	 */
	IMergeProcessStarted onLocalDiff(ProceedWithMergeHook hook);

	/**
	 * A custom callback called after the Merge Process finished its operation. This is specifically
	 * helpful in client deployment scenarios with an active UI thread: In those cases the Merge
	 * Process is an asynchronous operation (to not block the UI thread on-the-run). Then this
	 * callback can be used to receive the "finish event" of the asynchronous task.
	 *
	 * @param callback The invoked callback after a successful acknowledgement from all participating
	 *        - potentially remote - data repositories
	 * @return The fluent-API handle for the current merge process instance on the stack. Call
	 *         {@link #finish()} to execute the configured merge
	 */
	IMergeProcessStarted onSuccess(MergeFinishedCallback callback);

	/**
	 * Gives the Merge Process a hint whether newly created entity instances should not be added to
	 * the current 1st level cache. The default value for this behavior is "true". This flag can be
	 * set to "false" in cases where you do large amounts of "fire and forget" entity creations and
	 * want save the efforts to treat them in the cache without any real need to further access
	 * exactly the given instances with the same thread. Note that even with "false" an ordinary
	 * cached behavior still applies when the entity is requested later by any thread. Regarding this
	 * flag being set to "true" (which again is the default) be also aware about consequences of using
	 * a THREAD_LOCAL 1st level cache in deployment scenarios where the Ambeth UI thread behavior is
	 * activated at the same time. If in those cases the initial call to the Merge Process is done
	 * from within the UI thread the entities get put to a thread-local cache instance owned by a
	 * worker thread (i.e. not owned by the UI-thread) which might not be what you want. This is
	 * another reason why a SINGLETON 1st level cache is recommended when using the Ambeth stack in
	 * deployments where a "UI-thread-aware behavior" (=not blocking the UI at any time) is also
	 * expected from Ambeth.
	 *
	 * @return The fluent-API handle for the current merge process instance on the stack. Call
	 *         {@link #finish()} to execute the configured merge
	 */
	IMergeProcessStarted suppressNewEntitiesAddedToCache();

	/**
	 * Gives the Merge Process the hint to consider all to-be-merged entities with a shallow merge
	 * behavior: That is the behavior where only the explicitly passed entities are analyzed for
	 * changes. The default is a deep-merge where each passed entity is working as a root where all
	 * initialized relations are also checked for their own cascaded dirty flag. This algorithm
	 * proceeds recursively as long as this flag is active on each path step. This can result in an
	 * arbitrary large implicit change on the model transition on a deep transitive path which is what
	 * might be intended or not depending on the application usecase. E.g. If you just want to process
	 * a narrow-scoped diff generated for an explicitly known set of entities. On such a scenario the
	 * use of {@link #shallow()} often goes together with {@link #onLocalDiff(ProceedWithMergeHook)}.
	 *
	 * @return The fluent-API handle for the current merge process instance on the stack. Call
	 *         {@link #finish()} to execute the configured merge
	 */
	IMergeProcessStarted shallow();
}
