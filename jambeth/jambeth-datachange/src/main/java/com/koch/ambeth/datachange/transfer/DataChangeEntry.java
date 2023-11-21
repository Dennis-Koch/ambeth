package com.koch.ambeth.datachange.transfer;

/*-
 * #%L
 * jambeth-datachange
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

import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

@XmlRootElement(name = "DataChangeEntry", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataChangeEntry implements IDataChangeEntry, IPrintable {
	@XmlElement
	protected Object id;

	@XmlElement
	protected byte idNameIndex;

	@XmlElement
	protected Class<?> entityType;

	@XmlElement
	protected Object version;

	@XmlElement
	protected String[] topics;

	public DataChangeEntry() {
		// Intended blank
	}

	public DataChangeEntry(Class<?> entityType, int idNameIndex, Object id, Object version) {
		this.entityType = entityType;
		this.idNameIndex = (byte) idNameIndex;
		this.id = id;
		this.version = version;
	}

	@Override
	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	@Override
	public byte getIdNameIndex() {
		return idNameIndex;
	}

	public void setIdNameIndex(byte idNameIndex) {
		this.idNameIndex = idNameIndex;
	}

	@Override
	public Class<?> getEntityType() {
		return entityType;
	}

	public void setEntityType(Class<?> entityType) {
		this.entityType = entityType;
	}

	@Override
	public Object getVersion() {
		return version;
	}

	public void setVersion(Object version) {
		this.version = version;
	}

	@Override
	public String[] getTopics() {
		return topics;
	}

	@Override
	public void setTopics(String[] topics) {
		this.topics = topics;
	}

	@Override
	public String toString() {
		return StringBuilderUtil.printPrintable(this);
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("EntityType=").append(getEntityType().getName()).append(" IdIndex=")
				.append(getIdNameIndex()).append(" Id=");
		StringBuilderUtil.appendPrintable(sb, getId());
		sb.append(" Version=").append(getVersion());
	}
}
