package com.koch.ambeth.persistence.sql;

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

import java.util.HashMap;

import com.koch.ambeth.persistence.api.ICursorItem;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.query.persistence.IVersionItem;

public class ResultSetCursorBase extends ResultSetVersionCursorBase implements ICursorItem {
	protected final HashMap<String, IFieldMetaData> memberNameToFieldDict =
			new HashMap<>();
	protected final HashMap<String, Integer> memberNameToFieldIndexDict =
			new HashMap<>();
	protected final HashMap<String, Integer> fieldNameToFieldIndexDict =
			new HashMap<>();

	protected Object[] values;
	protected IFieldMetaData[] fields;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		IFieldMetaData[] fields = this.fields;
		values = new Object[fields.length];
		for (int a = fields.length; a-- > 0;) {
			IFieldMetaData field = fields[a];
			Integer index = Integer.valueOf(a + systemColumnCount);
			if (field.getMember() != null) {
				String memberName = field.getMember().getName();
				memberNameToFieldDict.put(memberName, field);
				memberNameToFieldIndexDict.put(memberName, index);
			}
			fieldNameToFieldIndexDict.put(field.getName(), index);
		}
	}

	public IFieldMetaData[] getFields() {
		return fields;
	}

	public void setFields(IFieldMetaData[] fields) {
		this.fields = fields;
	}

	@Override
	public Object[] getValues() {
		return values;
	}

	@Override
	public ICursorItem next() {
		IVersionItem item = super.next();
		if (item == null) {
			return null;
		}
		return this;
	}

	@Override
	protected void processResultSetItem(Object[] current) {
		super.processResultSetItem(current);
		IFieldMetaData[] fields = this.fields;
		int systemColumnCount = this.systemColumnCount;
		Object[] values = getValues();
		for (int a = fields.length; a-- > 0;) {
			values[a] = current[a + systemColumnCount];
		}
	}

	public IFieldMetaData getFieldByMemberName(String memberName) {
		return memberNameToFieldDict.get(memberName);
	}

	public int getFieldIndexByMemberName(String memberName) {
		return memberNameToFieldIndexDict.get(memberName).intValue();
	}

	public int getFieldIndexByName(String fieldName) {
		return fieldNameToFieldIndexDict.get(fieldName).intValue();
	}
}
