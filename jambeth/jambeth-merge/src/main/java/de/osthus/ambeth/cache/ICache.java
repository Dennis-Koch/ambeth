package de.osthus.ambeth.cache;

import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.Lock;

public interface ICache
{
	/**
	 * Alternative interface for
	 * 
	 * IList<Object> getObjects(List<IObjRef>, Set<CacheDirective>)
	 * 
	 * @param type
	 *            Type of objects to get.
	 * @param ids
	 *            IDs of objects to get.
	 * @return Typed list of loaded objects.
	 */
	<E> IList<E> getObjects(Class<E> type, Object... ids);

	/**
	 * Alternative interface for
	 * 
	 * IList<Object> getObjects(List<IObjRef>, Set<CacheDirective>)
	 * 
	 * @param type
	 *            Type of objects to get.
	 * @param ids
	 *            IDs of objects to get.
	 * @return Typed list of loaded objects.
	 */
	<E> IList<E> getObjects(Class<E> type, List<?> ids);

	/**
	 * Alternative interface for
	 * 
	 * IList<Object> getObjects(List<IObjRef>, Set<CacheDirective>)
	 * 
	 * @param orisToGetArray
	 *            Objects references for the objects to get.
	 * @param cacheDirective
	 *            Set of cache directives.
	 * @return List of loaded objects.
	 */
	IList<Object> getObjects(IObjRef[] orisToGetArray, Set<CacheDirective> cacheDirective);

	/**
	 * Getter for objects from cache, identified by object references. The cache may cascade the request to secondary caches or other persistence layers.
	 * 
	 * @param orisToGet
	 *            Object references for the objects to get.
	 * @param cacheDirective
	 *            Set of cache directives.
	 * @return List of loaded objects.
	 */
	IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective);

	/**
	 * Getter for objects from cache, identified by object relation. The cache may cascade the request to secondary caches or other persistence layers.
	 * 
	 * @param self
	 *            A specific member of an object reference, wanting to know what its value in context of its parent object is.
	 * @param cacheDirective
	 *            Set of cache directives.
	 * @return List of loaded objects.
	 */
	IList<IObjRelationResult> getObjRelations(List<IObjRelation> self, Set<CacheDirective> cacheDirective);

	/**
	 * Getter for one object from cache, identified by an object reference. The cache may cascade the request to secondary caches or other persistence layers.
	 * 
	 * @param oriToGet
	 *            Object reference for the object to get.
	 * @param cacheDirective
	 *            Set of cache directives.
	 * @return Requested object or null.
	 */
	Object getObject(IObjRef oriToGet, Set<CacheDirective> cacheDirective);

	/**
	 * Alternative interface for
	 * 
	 * Object getObject(IObjRef, Set<CacheDirective>)
	 * 
	 * @param type
	 *            Type of object to get.
	 * @param id
	 *            ID of objects to get.
	 * @return Requested object or null.
	 */
	<E> E getObject(Class<E> type, Object id);

	/**
	 * Convenience method for <E> E getObject(Class<E> type, Object id) It is able to create the correct compositeId instance internally. It should not be used
	 * if the id is not a composite id
	 * 
	 * @param <E>
	 * @param type
	 * @param compositeIdParts
	 * @return
	 */
	<E> E getObject(Class<E> type, Object... compositeIdParts);

	<E> E getObject(Class<E> type, String idName, Object id);

	/**
	 * Alternative interface for
	 * 
	 * Object getObject(IObjRef, Set<CacheDirective>)
	 * 
	 * @param type
	 *            Type of object to get.
	 * @param id
	 *            ID of objects to get.
	 * @param cacheDirective
	 *            Set of cache directives.
	 * @return Requested object or null.
	 */
	<E> E getObject(Class<E> type, Object id, Set<CacheDirective> cacheDirective);

	<E> E getObject(Class<E> type, String idName, Object id, Set<CacheDirective> cacheDirective);

	/**
	 * Access point to execute a delegate method with every element in the cache.
	 * 
	 * @param handleContentDelegate
	 *            Delegate method.
	 */
	void getContent(HandleContentDelegate handleContentDelegate);

	Lock getReadLock();

	Lock getWriteLock();

	void cascadeLoadPath(Class<?> entityType, String cascadeLoadPath);
}