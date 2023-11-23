package com.koch.ambeth.merge.cache;

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

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.collections.IList;

import java.util.List;
import java.util.Set;

public interface ICache {
    /**
     * Checks whether this cache instance provides security-filtered or unfiltered content
     *
     * @return true if this cache instance provides unfiltered content
     */
    boolean isPrivileged();

    /**
     * If the given cache instance is some kind of proxy or thread-local implementation this method
     * retrieves the internally bound real cache instance. This method is intended to be used for
     * performance critical algorithms where multiple calls through the proxy implementation can be
     * skipped.<br>
     * <br>
     * CAUTION: The resulting instance is not intended to be used solely within the method which did
     * this call. To be precise: Do NOT pass the resulting instance to any other method or bean as an
     * argument and do NOT store the resulting instance on an object field. Leave it solely as a
     * method stack variable
     *
     * @return The proxied or thread-local target instance of a cache.
     */
    ICache getCurrentCache();

    /**
     * Alternative interface for
     * <p>
     * IList<Object> getObjects(List<IObjRef>, Set<CacheDirective>)
     *
     * @param type Type of objects to get.
     * @param ids  IDs of objects to get.
     * @return Typed list of loaded objects.
     */
    <E> IList<E> getObjects(Class<E> type, Object... ids);

    /**
     * Alternative interface for
     * <p>
     * IList<Object> getObjects(List<IObjRef>, Set<CacheDirective>)
     *
     * @param type Type of objects to get.
     * @param ids  IDs of objects to get.
     * @return Typed list of loaded objects.
     */
    <E> IList<E> getObjects(Class<E> type, List<?> ids);

    /**
     * Alternative interface for
     * <p>
     * IList<Object> getObjects(List<IObjRef>, Set<CacheDirective>)
     *
     * @param orisToGetArray Objects references for the objects to get.
     * @param cacheDirective Set of cache directives.
     * @return List of loaded objects.
     */
    IList<Object> getObjects(IObjRef[] orisToGetArray, Set<CacheDirective> cacheDirective);

    /**
     * Getter for objects from cache, identified by object references. The cache may cascade the
     * request to secondary caches or other persistence layers.
     *
     * @param orisToGet      Object references for the objects to get.
     * @param cacheDirective Set of cache directives.
     * @return List of loaded objects.
     */
    IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective);

    /**
     * Getter for objects from cache, identified by object relation. The cache may cascade the request
     * to secondary caches or other persistence layers.
     *
     * @param self           A specific member of an object reference, wanting to know what its value in context
     *                       of its parent object is.
     * @param cacheDirective Set of cache directives.
     * @return List of loaded objects.
     */
    IList<IObjRelationResult> getObjRelations(List<IObjRelation> self, Set<CacheDirective> cacheDirective);

    /**
     * Getter for one object from cache, identified by an object reference. The cache may cascade the
     * request to secondary caches or other persistence layers.
     *
     * @param oriToGet       Object reference for the object to get.
     * @param cacheDirective Set of cache directives.
     * @return Requested object or null.
     */
    Object getObject(IObjRef oriToGet, Set<CacheDirective> cacheDirective);

    /**
     * Alternative interface for
     * <p>
     * Object getObject(IObjRef, Set<CacheDirective>)
     *
     * @param type Type of object to get.
     * @param id   ID of objects to get.
     * @return Requested object or null.
     */
    <E> E getObject(Class<E> type, Object id);

    /**
     * Convenience method for <E> E getObject(Class<E> type, Object id) It is able to create the
     * correct compositeId instance internally. It should not be used if the id is not a composite id
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
     * <p>
     * Object getObject(IObjRef, Set<CacheDirective>)
     *
     * @param type           Type of object to get.
     * @param id             ID of objects to get.
     * @param cacheDirective Set of cache directives.
     * @return Requested object or null.
     */
    <E> E getObject(Class<E> type, Object id, Set<CacheDirective> cacheDirective);

    <E> E getObject(Class<E> type, String idName, Object id, Set<CacheDirective> cacheDirective);

    /**
     * Access point to execute a delegate method with every element in the cache.
     *
     * @param handleContentDelegate Delegate method.
     */
    void getContent(HandleContentDelegate handleContentDelegate);

    Lock getReadLock();

    Lock getWriteLock();

    void cascadeLoadPath(Class<?> entityType, String cascadeLoadPath);
}
