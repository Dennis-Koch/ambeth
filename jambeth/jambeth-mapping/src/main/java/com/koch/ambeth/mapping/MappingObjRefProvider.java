package com.koch.ambeth.mapping;

/*-
 * #%L
 * jambeth-mapping
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

import com.koch.ambeth.merge.IObjRefProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;

public class MappingObjRefProvider implements IObjRefProvider {
    protected final Member refBOBuidMember;

    protected final Member refBOVersionMember;

    protected final byte refBOBuidIndex;

    public MappingObjRefProvider(Member refBOBuidMember, Member refBOVersionMember, byte refBOBuidIndex) {
        this.refBOBuidMember = refBOBuidMember;
        this.refBOVersionMember = refBOVersionMember;
        this.refBOBuidIndex = refBOBuidIndex;
    }

    @Override
    public IObjRef getObjRef(Object obj, IEntityMetaData metaData) {
        var buid = refBOBuidMember.getValue(obj, false);
        var version = refBOVersionMember != null ? refBOVersionMember.getValue(obj, true) : null;
        var ori = new ObjRef(metaData.getEntityType(), refBOBuidIndex, buid, version);
        return ori;
    }
}
