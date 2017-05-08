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

import java.util.regex.Pattern;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.IPermissionGroup;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.util.IPrintable;

public class PermissionGroup implements IPermissionGroup, IPrintable {
	public static final String permGroupPrefix = "";

	public static final String permGroupSuffix = "_PG";

	public static final String permGroupIdNameOfData = "PERM_GROUP";

	public static final Pattern permGroupFieldForLink = Pattern.compile("(.+)_PG");

	public static final String permGroupIdName = "PERM_GROUP_ID";

	public static final String readPermColumName = "READ";

	public static final String updatePermColumName = "UPDATE";

	public static final String deletePermColumName = "DELETE";

	public static final String userIdName = "USER_ID";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property
	protected IFieldMetaData readPermissionField;

	@Property
	protected IFieldMetaData updatePermissionField;

	@Property
	protected IFieldMetaData deletePermissionField;

	@Property
	protected IFieldMetaData permissionGroupField;

	@Property
	protected IFieldMetaData userField;

	@Property
	protected IFieldMetaData permissionGroupFieldOnTarget;

	@Property
	protected ITableMetaData targetTable;

	@Property
	protected ITableMetaData table;

	@Override
	public ITableMetaData getTable() {
		return table;
	}

	@Override
	public ITableMetaData getTargetTable() {
		return targetTable;
	}

	@Override
	public IFieldMetaData getPermissionGroupFieldOnTarget() {
		return permissionGroupFieldOnTarget;
	}

	@Override
	public IFieldMetaData getUserField() {
		return userField;
	}

	@Override
	public IFieldMetaData getPermissionGroupField() {
		return permissionGroupField;
	}

	@Override
	public IFieldMetaData getReadPermissionField() {
		return readPermissionField;
	}

	@Override
	public IFieldMetaData getUpdatePermissionField() {
		return updatePermissionField;
	}

	@Override
	public IFieldMetaData getDeletePermissionField() {
		return deletePermissionField;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(getClass().getSimpleName()).append(": ").append(getTable().getName())
				.append(" applied to ").append(getTargetTable().getName()).append('.')
				.append(getPermissionGroupFieldOnTarget().getName());
	}
}
