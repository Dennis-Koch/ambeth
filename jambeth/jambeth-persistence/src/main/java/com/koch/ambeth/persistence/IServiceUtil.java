package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.merge.model.IObjRef;

import java.util.List;

public interface IServiceUtil {
    /**
     * TODO JavaDoc comment.
     *
     * @param targetEntities
     * @param entityType
     * @param cursor
     */
    <T> void loadObjects(List<T> targetEntities, Class<T> entityType, ILinkCursor cursor);

    /**
     * TODO JavaDoc comment.
     * <p>
     * Loads the instances of the objects selected in the cursor into the target entities list. This
     * is the step from having a search result to having the actual objects searched for.
     *
     * @param targetEntities List to store the target objects in.
     * @param entityType     Type of the entities to load.
     * @param cursor         Version cursor with the IDs and versions of the objects to load.
     */
    <T> void loadObjectsIntoCollection(List<T> targetEntities, Class<T> entityType, IVersionCursor cursor);

    /**
     * TODO JavaDoc comment.
     *
     * @param entityType Type of the entity to load.
     * @param item       Version info (ID and version) of the object to load.
     * @return If successful Requested object, otherwise null.
     */
    <T> T loadObject(Class<T> entityType, IVersionItem item);

    List<IObjRef> loadObjRefs(Class<?> entityType, int idIndex, IVersionCursor cursor);
}
