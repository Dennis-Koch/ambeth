package com.koch.ambeth.cache.transfer;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.model.IObjRef;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.List;

@XmlRootElement(name = "LoadContainer", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class LoadContainer implements ILoadContainer {
    @XmlElement(required = true)
    protected IObjRef reference;

    @XmlElement(required = true)
    protected Object[] primitives;

    @XmlElement(required = true)
    protected IObjRef[][] relations;

    @XmlTransient
    protected List<IObjRef>[] relationBuilds;

    @Override
    public IObjRef getReference() {
        return reference;
    }

    public void setReference(IObjRef reference) {
        this.reference = reference;
    }

    @Override
    public Object[] getPrimitives() {
        return primitives;
    }

    @Override
    public void setPrimitives(Object[] primitives) {
        this.primitives = primitives;
    }

    @Override
    public IObjRef[][] getRelations() {
        return relations;
    }

    public void setRelations(IObjRef[][] relations) {
        this.relations = relations;
    }

    public List<IObjRef>[] getRelationBuilds() {
        return relationBuilds;
    }

    public void setRelationBuilds(List<IObjRef>[] relationBuilds) {
        this.relationBuilds = relationBuilds;
    }
}
