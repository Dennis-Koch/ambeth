package com.koch.ambeth.merge.transfer;

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

import com.koch.ambeth.service.metadata.IDTOType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "EntityMetaDataTransfer", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class EntityMetaDataTransfer implements IDTOType {
    @XmlElement
    protected Class<?> entityType;

    @XmlElement
    protected Class<?>[] typesRelatingToThis;

    @XmlElement
    protected Class<?>[] typesToCascadeDelete;

    @XmlElement
    protected String[] relationMemberNames;

    @XmlElement
    protected String[] primitiveMemberNames;

    @XmlElement
    protected String[] alternateIdMemberNames;

    @XmlElement
    protected String versionMemberName;

    @XmlElement
    protected String idMemberName;

    @XmlElement
    protected int[][] alternateIdMemberIndicesInPrimitives;

    @XmlElement
    protected String createdOnMemberName;

    @XmlElement
    protected String createdByMemberName;

    @XmlElement
    protected String updatedOnMemberName;

    @XmlElement
    protected String updatedByMemberName;

    @XmlElement
    protected String[] mergeRelevantNames;
}
