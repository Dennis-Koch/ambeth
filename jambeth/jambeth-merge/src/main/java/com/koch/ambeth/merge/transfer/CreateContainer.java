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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.merge.model.ICreateOrUpdateContainer;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.util.Arrays;

@XmlRootElement(name = "CreateContainer", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateContainer extends AbstractChangeContainer implements ICreateOrUpdateContainer {
	public static final PrimitiveUpdateItem[] emptyPrimitiveItems = new PrimitiveUpdateItem[0];

	public static final RelationUpdateItem[] emptyRelationItems = new RelationUpdateItem[0];

	@XmlElement(required = true)
	protected IPrimitiveUpdateItem[] primitives;

	@XmlElement(required = true)
	protected IRelationUpdateItem[] relations;

	public IPrimitiveUpdateItem[] getPrimitives() {
		return primitives;
	}

	public void setPrimitives(IPrimitiveUpdateItem[] primitives) {
		this.primitives = primitives;
	}

	public IRelationUpdateItem[] getRelations() {
		return relations;
	}

	public void setRelations(IRelationUpdateItem[] relations) {
		this.relations = relations;
	}

	@Override
	public IPrimitiveUpdateItem[] getFullPUIs() {
		return getPrimitives();
	}

	@Override
	public IRelationUpdateItem[] getFullRUIs() {
		return getRelations();
	}

	@Override
	public void toString(StringBuilder sb) {
		super.toString(sb);
		sb.append(" Primitives=");
		Arrays.toString(sb, primitives);
		sb.append(" Relations=");
		Arrays.toString(sb, relations);
	}
}
