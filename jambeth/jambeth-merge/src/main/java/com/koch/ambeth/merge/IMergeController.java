package com.koch.ambeth.merge;

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

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.RelationUpdateItemBuild;
import com.koch.ambeth.merge.util.ValueHolderRef;
import com.koch.ambeth.service.merge.model.IObjRef;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IMergeController {
    void applyChangesToOriginals(ICUDResult cudResult, IOriCollection oriCollection, Function<Object, ICache> entityToAssociatedCaches);

    ICUDResult mergeDeep(Object obj, MergeHandle handle);

    IRelationUpdateItem createRUI(String memberName, List<IObjRef> oldOriList, List<IObjRef> newOriList);

    RelationUpdateItemBuild createRUIBuild(String memberName, List<IObjRef> oldOriList, List<IObjRef> newOriList);

    List<Object> scanForInitializedObjects(Object obj, boolean isDeepMerge, boolean dirtyOnly, Map<Class<?>, List<Object>> typeToObjectsToMerge, List<IObjRef> objRefs, List<IObjRef> privilegedObjRefs,
            List<ValueHolderRef> valueHolderRefs, Map<Object, ICache> entityToAssociatedCaches);
}
