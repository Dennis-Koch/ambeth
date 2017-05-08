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
import javax.xml.bind.annotation.XmlSeeAlso;

import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

@XmlRootElement(name = "AbstractChangeContainer", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({CreateContainer.class, UpdateContainer.class, DeleteContainer.class})
public abstract class AbstractChangeContainer implements IChangeContainer, IPrintable {
	@XmlElement(required = true)
	protected IObjRef reference;

	@Override
	public IObjRef getReference() {
		return reference;
	}

	@Override
	public void setReference(IObjRef reference) {
		this.reference = reference;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(getClass().getSimpleName()).append(": ");
		StringBuilderUtil.appendPrintable(sb, reference);
	}
}
