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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

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
	protected IList<IObjRef>[] relationBuilds;

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

	public IList<IObjRef>[] getRelationBuilds() {
		return relationBuilds;
	}

	public void setRelationBuilds(IList<IObjRef>[] relationBuilds) {
		this.relationBuilds = relationBuilds;
	}
}
