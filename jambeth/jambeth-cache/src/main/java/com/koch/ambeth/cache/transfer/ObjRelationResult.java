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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IObjRef;

@XmlRootElement(name = "ObjRelationResult", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjRelationResult implements IObjRelationResult {
	@XmlElement(required = true)
	protected IObjRelation reference;

	@XmlElement(required = true)
	protected IObjRef[] relations;

	@Override
	public IObjRelation getReference() {
		return reference;
	}

	public void setReference(IObjRelation reference) {
		this.reference = reference;
	}

	@Override
	public IObjRef[] getRelations() {
		return relations;
	}

	public void setRelations(IObjRef[] relations) {
		this.relations = relations;
	}
}
