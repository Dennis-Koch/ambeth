package com.koch.ambeth.security.server.service;

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

import com.koch.ambeth.security.server.privilege.evaluation.impl.EntityPermissionEvaluation;
import com.koch.ambeth.security.server.privilege.evaluation.impl.EntityPropertyPermissionEvaluation;
import com.koch.ambeth.security.server.privilege.evaluation.impl.ScopedEntityPermissionEvaluation;
import com.koch.ambeth.security.server.privilege.evaluation.impl.ScopedEntityPropertyPermissionEvaluation;

public class PrivilegeHandle
{
	public Boolean create;

	public Boolean read;

	public Boolean update;

	public Boolean delete;

	public Boolean execute;

	public void applyIfNull(ScopedEntityPermissionEvaluation spe)
	{
		if (spe == null)
		{
			return;
		}
		create = spe.getCreate();
		read = spe.getRead();
		update = spe.getUpdate();
		delete = spe.getDelete();
		execute = spe.getExecute();
	}

	public void applyIfNull(EntityPermissionEvaluation pe)
	{
		if (pe == null)
		{
			return;
		}
		if (create == null)
		{
			create = pe.getCreate();
		}
		if (read == null)
		{
			read = pe.getRead();
		}
		if (update == null)
		{
			update = pe.getUpdate();
		}
		if (delete == null)
		{
			delete = pe.getDelete();
		}
		if (execute == null)
		{
			execute = pe.getExecute();
		}
	}

	public void applyIfNull(boolean create, boolean read, boolean update, boolean delete, boolean execute)
	{
		if (this.create == null)
		{
			this.create = Boolean.valueOf(create);
		}
		if (this.read == null)
		{
			this.read = Boolean.valueOf(read);
		}
		if (this.update == null)
		{
			this.update = Boolean.valueOf(update);
		}
		if (this.delete == null)
		{
			this.delete = Boolean.valueOf(delete);
		}
		if (this.execute == null)
		{
			this.execute = Boolean.valueOf(execute);
		}
	}

	public void applyPropertySpecifics(ScopedEntityPermissionEvaluation spe, String propertyName)
	{
		if (spe == null)
		{
			return;
		}
		ScopedEntityPropertyPermissionEvaluation sppe = spe.getPropertyPermissions().get(propertyName);
		if (sppe == null)
		{
			return;
		}
		if (create == null)
		{
			create = sppe.getCreate();
		}
		if (read == null)
		{
			read = sppe.getRead();
		}
		if (update == null)
		{
			update = sppe.getUpdate();
		}
		if (delete == null)
		{
			delete = sppe.getDelete();
		}
	}

	public void applyPropertySpecifics(EntityPermissionEvaluation pe, String propertyName)
	{
		if (pe == null)
		{
			return;
		}
		EntityPropertyPermissionEvaluation ppe = pe.getPropertyPermissions().get(propertyName);
		if (ppe == null)
		{
			return;
		}
		if (create == null)
		{
			create = ppe.getCreate();
		}
		if (read == null)
		{
			read = ppe.getRead();
		}
		if (update == null)
		{
			update = ppe.getUpdate();
		}
		if (delete == null)
		{
			delete = ppe.getDelete();
		}
	}

	public void applyIfNull(PrivilegeHandle ph)
	{
		if (create == null)
		{
			create = ph.create;
		}
		if (read == null)
		{
			read = ph.read;
		}
		if (update == null)
		{
			update = ph.update;
		}
		if (delete == null)
		{
			delete = ph.delete;
		}
		if (execute == null)
		{
			execute = ph.execute;
		}
	}
}
