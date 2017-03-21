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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.IPrintable;

@XmlRootElement(name = "RelationUpdateItem", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class RelationUpdateItem implements IRelationUpdateItem, IPrintable {
	@XmlElement(required = true)
	protected String memberName;

	@XmlElement(required = false)
	protected IObjRef[] addedORIs;

	@XmlElement(required = false)
	protected IObjRef[] removedORIs;

	@Override
	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	@Override
	public IObjRef[] getAddedORIs() {
		return addedORIs;
	}

	public void setAddedORIs(IObjRef[] addedORIs) {
		this.addedORIs = addedORIs;
	}

	@Override
	public IObjRef[] getRemovedORIs() {
		return removedORIs;
	}

	public void setRemovedORIs(IObjRef[] removedORIs) {
		this.removedORIs = removedORIs;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("RUI: MemberName=").append(getMemberName());
		IObjRef[] addedORIs = getAddedORIs();
		IObjRef[] removedORIs = getRemovedORIs();
		if (addedORIs != null && addedORIs.length > 0) {
			sb.append(" AddedORIs=");
			Arrays.toString(sb, addedORIs);
		}
		if (removedORIs != null && removedORIs.length > 0) {
			sb.append(" RemovedORIs=");
			Arrays.toString(sb, removedORIs);
		}
	}
}
