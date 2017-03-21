package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.config.IProperties;

public class FieldMetaData implements IFieldMetaData, IInitializingBean {
	protected IProperties properties;

	protected ITableMetaData table;

	protected String name;

	protected Member member;

	protected Class<?> fieldType;

	protected Class<?> fieldSubType;

	protected boolean isAlternateId;

	protected boolean expectsMapping = true;

	protected byte idIndex = ObjRef.UNDEFINED_KEY_INDEX;

	protected int indexOnTable = -1;

	protected String originalTypeName;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(properties, "properties");
		ParamChecker.assertNotNull(table, "table");
		ParamChecker.assertNotNull(name, "name");
		ParamChecker.assertNotNull(fieldType, "fieldType");
	}

	public void setProperties(IProperties properties) {
		this.properties = properties;
	}

	@Override
	public ITableMetaData getTable() {
		return table;
	}

	public void setTable(ITableMetaData table) {
		this.table = table;
	}

	@Override
	public boolean expectsMapping() {
		return expectsMapping;
	}

	public void setExpectsMapping(boolean expectsMapping) {
		this.expectsMapping = expectsMapping;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isAlternateId() {
		return isAlternateId;
	}

	public void setAlternateId() {
		isAlternateId = true;
	}

	@Override
	public byte getIdIndex() {
		return idIndex;
	}

	public void setIdIndex(byte idIndex) {
		this.idIndex = idIndex;
	}

	@Override
	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		if (this.member == member) {
			return;
		}
		if (this.member != null && !this.member.getName().equals(member.getName())) {
			throw new IllegalStateException(
					"Member already configured and can not be changed later. A call to this method here is a bug");
		}
		this.member = member;
	}

	@Override
	public Class<?> getFieldType() {
		return fieldType;
	}

	public void setFieldType(Class<?> fieldType) {
		this.fieldType = fieldType;
	}

	public void setFieldSubType(Class<?> fieldSubType) {
		this.fieldSubType = fieldSubType;
	}

	@Override
	public Class<?> getFieldSubType() {
		return fieldSubType;
	}

	@Override
	public Class<?> getEntityType() {
		if (table == null) {
			return null;
		}
		return table.getEntityType();
	}

	@Override
	public String toString() {
		return "Field: " + getName();
	}

	@Override
	public int getIndexOnTable() {
		return indexOnTable;
	}

	public void setIndexOnTable(int indexOnTable) {
		this.indexOnTable = indexOnTable;
	}

	@Override
	public String getOriginalTypeName() {
		return originalTypeName;
	}

	public void setOriginalTypeName(String originalTypeName) {
		this.originalTypeName = originalTypeName;
	}
}
