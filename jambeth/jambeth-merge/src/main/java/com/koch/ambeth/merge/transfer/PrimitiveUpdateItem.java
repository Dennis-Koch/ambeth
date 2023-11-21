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

import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.util.IPrintable;

@XmlRootElement(name = "PrimitiveUpdateItem", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrimitiveUpdateItem implements IPrimitiveUpdateItem, IPrintable {
	@XmlElement(required = true)
	public Object newValue;

	@XmlElement(required = true)
	public String memberName;

	public PrimitiveUpdateItem() {
		// intended blank
	}

	public PrimitiveUpdateItem(String memberName, Object newValue) {
		this.memberName = memberName;
		this.newValue = newValue;
	}

	@Override
	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	@Override
	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("PUI: MemberName=").append(getMemberName()).append(" NewValue='")
				.append(getNewValue()).append('\'');
	}
}
