package com.koch.ambeth.security.persistence;

/*-
 * #%L
 * jambeth-security-persistence
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

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.persistence.api.IPermissionGroup;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public class PgUpdateEntry {
	protected final IPermissionGroup permissionGroup;

	protected final Class<?> entityType;

	protected PermissionGroupUpdateType updateType = PermissionGroupUpdateType.NOTHING;

	protected IDataChange dataChange;

	protected IList<IObjRef> objRefs;

	protected IList<Object> permissionGroupIds;

	protected int startIndexInAllObjRefs;

	public PgUpdateEntry(Class<?> entityType, IPermissionGroup permissionGroup) {
		this.entityType = entityType;
		this.permissionGroup = permissionGroup;
	}

	public IPermissionGroup getPermissionGroup() {
		return permissionGroup;
	}

	public Class<?> getEntityType() {
		return entityType;
	}

	public PermissionGroupUpdateType getUpdateType() {
		return updateType;
	}

	public void setUpdateType(PermissionGroupUpdateType updateType) {
		this.updateType = updateType;
	}

	public IDataChange getDataChange() {
		return dataChange;
	}

	public void setDataChange(IDataChange dataChange) {
		this.dataChange = dataChange;
	}

	public IList<IObjRef> getObjRefs() {
		return objRefs;
	}

	public void setObjRefs(IList<IObjRef> objRefs) {
		this.objRefs = objRefs;
	}

	public IList<Object> getPermissionGroupIds() {
		return permissionGroupIds;
	}

	public void setPermissionGroupIds(IList<Object> permissionGroupIds) {
		this.permissionGroupIds = permissionGroupIds;
	}

	@Override
	public String toString() {
		return getUpdateType().toString();
	}

	public int getStartIndexInAllObjRefs() {
		return startIndexInAllObjRefs;
	}

	public void setStartIndexInAllObjRefs(int startIndexInAllObjRefs) {
		this.startIndexInAllObjRefs = startIndexInAllObjRefs;
	}
}
