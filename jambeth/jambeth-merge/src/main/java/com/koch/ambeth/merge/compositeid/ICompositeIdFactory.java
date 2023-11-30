package com.koch.ambeth.merge.compositeid;

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

import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.PrimitiveMember;

public interface ICompositeIdFactory {
    PrimitiveMember createCompositeIdMember(IEntityMetaData metaData, PrimitiveMember[] idMembers);

    PrimitiveMember createCompositeIdMember(Class<?> entityType, PrimitiveMember[] idMembers);

    Object createCompositeId(IEntityMetaData metaData, PrimitiveMember compositeIdMember, Object... ids);

    Object createCompositeId(IEntityMetaData metaData, int idIndex, Object... ids);

    Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, Object[] primitives);

    Object createIdFromPrimitives(IEntityMetaData metaData, int idIndex, AbstractCacheValue cacheValue);

    Object createIdFromEntity(IEntityMetaData metaData, int idIndex, Object entity);
}
