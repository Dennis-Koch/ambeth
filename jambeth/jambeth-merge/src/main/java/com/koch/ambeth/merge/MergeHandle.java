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

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.model.IUpdateItem;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import lombok.Getter;
import lombok.Setter;

public class MergeHandle implements IObjRefHelper.IObjRefHelperState {
    public static final String P_CACHE = "Cache";

    public static final String P_DEEP_MERGE = "DeepMerge";

    public static final String P_PRIVILEGED_CACHE = "PrivilegedCache";

    @Getter
    protected final IdentityHashSet<Object> alreadyProcessedSet = new IdentityHashSet<>();

    @Getter
    protected final IdentityHashMap<Object, IObjRef> objToObjRefMap = new IdentityHashMap<>();

    @Getter
    protected final HashMap<IObjRef, Object> objRefToObjMap = new HashMap<>();

    protected final IdentityLinkedMap<Object, IList<IUpdateItem>> objToModDict = new IdentityLinkedMap<>();

    @Getter
    protected final ArrayList<IObjRef> oldOrList = new ArrayList<>();

    @Getter
    protected final ArrayList<IObjRef> newOrList = new ArrayList<>();

    @Getter
    protected final IdentityHashSet<Object> objToDeleteSet = new IdentityHashSet<>();

    @Getter
    protected final ArrayList<Object> pendingValueHolders = new ArrayList<>();

    @Getter
    protected final ArrayList<Runnable> pendingRunnables = new ArrayList<>();

    @Getter
    @Setter
    protected IdentityHashMap<Object, ICache> entityToAssociatedCaches;

    @Getter
    @Setter
    protected ICache cache;

    @Getter
    @Setter
    protected ICache privilegedCache;

    @Getter
    @Setter
    protected boolean isCacheToDispose;

    @Getter
    @Setter
    protected boolean isPrivilegedCacheToDispose;

    @Getter
    @Property(name = MergeConfigurationConstants.FieldBasedMergeActive, defaultValue = "true")
    protected boolean fieldBasedMergeActive;

    @Getter
    @Setter
    protected boolean handleExistingIdAsNewId;

    @Getter
    @Setter
    protected boolean isDeepMerge = true;
}
