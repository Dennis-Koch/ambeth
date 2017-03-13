package com.koch.ambeth.security.server.privilege.evaluation.impl;

import java.util.Map.Entry;

import com.koch.ambeth.security.server.privilege.evaluation.IScopedCreateEntityPropertyStep;
import com.koch.ambeth.security.server.privilege.evaluation.IScopedCreateEntityStep;
import com.koch.ambeth.security.server.privilege.evaluation.IScopedDeleteEntityStep;
import com.koch.ambeth.security.server.privilege.evaluation.IScopedEntityPermissionEvaluation;
import com.koch.ambeth.security.server.privilege.evaluation.IScopedExecuteEntityStep;
import com.koch.ambeth.security.server.privilege.evaluation.IScopedUpdateEntityStep;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;

public class ScopedEntityPermissionEvaluation implements IScopedEntityPermissionEvaluation, IScopedCreateEntityStep, IScopedUpdateEntityStep,
		IScopedDeleteEntityStep, IScopedExecuteEntityStep
{
	protected Boolean create, read, update, delete, execute;

	protected final ArrayList<ScopedEntityPropertyPermissionEvaluation> unusedPropertyPermissions = new ArrayList<ScopedEntityPropertyPermissionEvaluation>();

	protected final HashMap<String, ScopedEntityPropertyPermissionEvaluation> propertyPermissions = new HashMap<String, ScopedEntityPropertyPermissionEvaluation>();

	protected final EntityPermissionEvaluation permissionEvaluation;

	public ScopedEntityPermissionEvaluation(EntityPermissionEvaluation permissionEvaluation)
	{
		this.permissionEvaluation = permissionEvaluation;
	}

	public HashMap<String, ScopedEntityPropertyPermissionEvaluation> getPropertyPermissions()
	{
		return propertyPermissions;
	}

	public Boolean getCreate()
	{
		return create;
	}

	public Boolean getRead()
	{
		return read;
	}

	public Boolean getUpdate()
	{
		return update;
	}

	public Boolean getDelete()
	{
		return delete;
	}

	public Boolean getExecute()
	{
		return execute;
	}

	public void reset()
	{
		create = null;
		read = null;
		update = null;
		delete = null;
		execute = null;
		for (Entry<String, ScopedEntityPropertyPermissionEvaluation> entry : propertyPermissions)
		{
			ScopedEntityPropertyPermissionEvaluation propertyPermission = entry.getValue();
			propertyPermission.reset();
			unusedPropertyPermissions.add(propertyPermission);
		}
		propertyPermissions.clear();
	}

	@Override
	public IScopedUpdateEntityStep allowCreate()
	{
		if (create == null || !permissionEvaluation.createTrueDefault)
		{
			create = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IScopedUpdateEntityStep skipCreate()
	{
		return this;
	}

	@Override
	public IScopedUpdateEntityStep denyCreate()
	{
		if (create == null || permissionEvaluation.createTrueDefault)
		{
			create = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IScopedCreateEntityStep allowRead()
	{
		if (read == null || !permissionEvaluation.readTrueDefault)
		{
			read = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public void denyRead()
	{
		if (read == null || permissionEvaluation.readTrueDefault)
		{
			read = Boolean.FALSE;
		}
	}

	@Override
	public IScopedDeleteEntityStep allowUpdate()
	{
		if (update == null || !permissionEvaluation.updateTrueDefault)
		{
			update = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IScopedDeleteEntityStep skipUpdate()
	{
		return this;
	}

	@Override
	public IScopedDeleteEntityStep denyUpdate()
	{
		if (update == null || permissionEvaluation.updateTrueDefault)
		{
			update = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IScopedExecuteEntityStep allowDelete()
	{
		if (delete == null || !permissionEvaluation.deleteTrueDefault)
		{
			delete = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IScopedExecuteEntityStep skipDelete()
	{
		return this;
	}

	@Override
	public IScopedExecuteEntityStep denyDelete()
	{
		if (delete == null || permissionEvaluation.deleteTrueDefault)
		{
			delete = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public void allowExecute()
	{
		if (execute == null || !permissionEvaluation.executeTrueDefault)
		{
			execute = Boolean.TRUE;
		}
	}

	@Override
	public void skipExecute()
	{
		// intended blank
	}

	@Override
	public void denyExecute()
	{
		if (execute == null || permissionEvaluation.executeTrueDefault)
		{
			execute = Boolean.FALSE;
		}
	}

	@Override
	public void allowEach()
	{
		allowRead().allowCreate().allowUpdate().allowDelete().allowExecute();
	}

	@Override
	public void skipEach()
	{
		// intended blank
	}

	@Override
	public void denyEach()
	{
		denyRead();
	}

	@Override
	public IScopedCreateEntityPropertyStep allowReadProperty(String propertyName)
	{
		// to read a single property it implies general read permission
		allowRead();
		ScopedEntityPropertyPermissionEvaluation propertyPermission = getPropertyPermission(propertyName);
		propertyPermission.allowReadProperty();
		return propertyPermission;
	}

	@Override
	public IScopedEntityPermissionEvaluation denyReadProperty(String propertyName)
	{
		ScopedEntityPropertyPermissionEvaluation propertyPermission = getPropertyPermission(propertyName);
		propertyPermission.denyReadProperty();
		return this;
	}

	protected ScopedEntityPropertyPermissionEvaluation getPropertyPermission(String propertyName)
	{
		ScopedEntityPropertyPermissionEvaluation propertyPermission = propertyPermissions.get(propertyName);
		if (propertyPermission != null)
		{
			return propertyPermission;
		}
		propertyPermission = unusedPropertyPermissions.popLastElement();
		if (propertyPermission == null)
		{
			propertyPermission = new ScopedEntityPropertyPermissionEvaluation(this);
		}
		propertyPermissions.put(propertyName, propertyPermission);
		return propertyPermission;
	}
}
