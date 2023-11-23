package com.koch.ambeth.merge.typeinfo;

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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.util.IImmutableTypeSet;
import com.koch.ambeth.service.metadata.IDTOType;
import com.koch.ambeth.util.collections.SmartCopySet;
import com.koch.ambeth.util.typeinfo.INoEntityTypeExtendable;
import com.koch.ambeth.util.typeinfo.IRelationProvider;
import jakarta.persistence.Embeddable;

import java.util.Arrays;

public class RelationProvider implements IRelationProvider, INoEntityTypeExtendable, IInitializingBean {
    protected final SmartCopySet<Class<?>> primitiveTypes = new SmartCopySet<>();
    protected final ClassExtendableContainer<Boolean> noEntityTypeExtendables = new ClassExtendableContainer<>("flag", "noEntityType");
    @Autowired
    protected IImmutableTypeSet immutableTypeSet;

    @Override
    public void afterPropertiesSet() throws Throwable {
        immutableTypeSet.addImmutableTypesTo(primitiveTypes);

        primitiveTypes.addAll(Arrays.asList(new Class<?>[] {
                Object.class, java.util.Date.class, java.sql.Date.class, java.sql.Timestamp.class, java.util.Calendar.class
        }));
        primitiveTypes.add(java.util.GregorianCalendar.class);
        primitiveTypes.add(javax.xml.datatype.XMLGregorianCalendar.class);

        noEntityTypeExtendables.register(Boolean.TRUE, IDTOType.class);
    }

    @Override
    public boolean isEntityType(Class<?> type) {
        if (type == null || immutableTypeSet.isImmutableType(type) || primitiveTypes.contains(type) || Boolean.TRUE == noEntityTypeExtendables.getExtension(type) || type.isAnnotationPresent(
                Embeddable.class)) {
            return false;
        }
        return true;
    }

    @Override
    public void registerNoEntityType(Class<?> noEntityType) {
        noEntityTypeExtendables.register(Boolean.TRUE, noEntityType);
    }

    @Override
    public void unregisterNoEntityType(Class<?> noEntityType) {
        noEntityTypeExtendables.unregister(Boolean.TRUE, noEntityType);
    }
}
