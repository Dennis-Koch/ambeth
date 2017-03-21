package com.koch.ambeth.security.server.privilege.evaluation.impl;

/*-
 * #%L
 * jambeth-security-server
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

import com.koch.ambeth.security.server.privilege.evaluation.IScopedCreateEntityPropertyStep;
import com.koch.ambeth.security.server.privilege.evaluation.IScopedDeleteEntityPropertyStep;
import com.koch.ambeth.security.server.privilege.evaluation.IScopedUpdateEntityPropertyStep;

public class ScopedEntityPropertyPermissionEvaluation implements IScopedCreateEntityPropertyStep,
		IScopedUpdateEntityPropertyStep, IScopedDeleteEntityPropertyStep {
	protected Boolean create, read, update, delete;

	protected final ScopedEntityPermissionEvaluation permissionEvaluation;

	public ScopedEntityPropertyPermissionEvaluation(
			ScopedEntityPermissionEvaluation permissionEvaluation) {
		this.permissionEvaluation = permissionEvaluation;
	}

	public Boolean getCreate() {
		return create;
	}

	public Boolean getRead() {
		return read;
	}

	public Boolean getUpdate() {
		return update;
	}

	public Boolean getDelete() {
		return delete;
	}

	public void reset() {
		create = null;
		read = null;
		update = null;
		delete = null;
	}

	public void allowReadProperty() {
		if (read == null || !permissionEvaluation.permissionEvaluation.readTrueDefault) {
			read = Boolean.TRUE;
		}
	}

	public void denyReadProperty() {
		if (read == null || permissionEvaluation.permissionEvaluation.readTrueDefault) {
			read = Boolean.FALSE;
		}
		denyCreateProperty();
		denyUpdateProperty();
		denyDeleteProperty();
	}

	@Override
	public void allowDeleteProperty() {
		if (delete == null || !permissionEvaluation.permissionEvaluation.deleteTrueDefault) {
			delete = Boolean.TRUE;
		}
	}

	@Override
	public void skipDeleteProperty() {
		// intended blank
	}

	@Override
	public void denyDeleteProperty() {
		if (delete == null || permissionEvaluation.permissionEvaluation.deleteTrueDefault) {
			delete = Boolean.FALSE;
		}
	}

	@Override
	public IScopedDeleteEntityPropertyStep allowUpdateProperty() {
		if (update == null || !permissionEvaluation.permissionEvaluation.updateTrueDefault) {
			update = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IScopedDeleteEntityPropertyStep skipUpdateProperty() {
		return this;
	}

	@Override
	public IScopedDeleteEntityPropertyStep denyUpdateProperty() {
		if (update == null || permissionEvaluation.permissionEvaluation.updateTrueDefault) {
			update = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IScopedUpdateEntityPropertyStep allowCreateProperty() {
		if (create == null || !permissionEvaluation.permissionEvaluation.createTrueDefault) {
			create = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IScopedUpdateEntityPropertyStep skipCreateProperty() {
		return this;
	}

	@Override
	public IScopedUpdateEntityPropertyStep denyCreateProperty() {
		if (create == null || permissionEvaluation.permissionEvaluation.createTrueDefault) {
			create = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public void allowCUDProperty() {
		allowCreateProperty().allowUpdateProperty().allowDeleteProperty();
	}

	@Override
	public void skipCUDProperty() {
		skipCreateProperty().skipUpdateProperty().skipDeleteProperty();
	}

	@Override
	public void denyCUDProperty() {
		denyCreateProperty().denyUpdateProperty().denyDeleteProperty();
	}
}
