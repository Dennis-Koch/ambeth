package com.koch.ambeth.security.server.privilege.evaluation.impl;

import java.util.Arrays;
import java.util.Map.Entry;

import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;
import com.koch.ambeth.security.server.privilege.evaluation.ICreateEntityPropertyStep;
import com.koch.ambeth.security.server.privilege.evaluation.ICreateEntityStep;
import com.koch.ambeth.security.server.privilege.evaluation.IDeleteEntityPropertyStep;
import com.koch.ambeth.security.server.privilege.evaluation.IDeleteEntityStep;
import com.koch.ambeth.security.server.privilege.evaluation.IEntityPermissionEvaluation;
import com.koch.ambeth.security.server.privilege.evaluation.IExecuteEntityStep;
import com.koch.ambeth.security.server.privilege.evaluation.IScopedEntityPermissionEvaluation;
import com.koch.ambeth.security.server.privilege.evaluation.IUpdateEntityPropertyStep;
import com.koch.ambeth.security.server.privilege.evaluation.IUpdateEntityStep;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;

public class EntityPermissionEvaluation implements IEntityPermissionEvaluation, ICreateEntityStep, IUpdateEntityStep, IDeleteEntityStep, IExecuteEntityStep
{
	protected final ISecurityScope[] scopes;

	protected final ScopedEntityPermissionEvaluation[] spes;

	protected final boolean createTrueDefault, readTrueDefault, updateTrueDefault, deleteTrueDefault, executeTrueDefault;

	protected final boolean createPropertyTrueDefault, readPropertyTrueDefault, updatePropertyTrueDefault, deletePropertyTrueDefault;

	protected final ArrayList<EntityPropertyPermissionEvaluation> unusedPropertyPermissions = new ArrayList<EntityPropertyPermissionEvaluation>();

	protected final HashMap<String, EntityPropertyPermissionEvaluation> propertyPermissions = new HashMap<String, EntityPropertyPermissionEvaluation>();

	protected Boolean create, read, update, delete, execute;

	public EntityPermissionEvaluation(ISecurityScope[] scopes, boolean createTrueDefault, boolean readTrueDefault, boolean updateTrueDefault,
			boolean deleteTrueDefault, boolean executeTrueDefault, boolean createPropertyTrueDefault, boolean readPropertyTrueDefault,
			boolean updatePropertyTrueDefault, boolean deletePropertyTrueDefault)
	{
		this.scopes = scopes;
		this.createTrueDefault = createTrueDefault;
		this.readTrueDefault = readTrueDefault;
		this.updateTrueDefault = updateTrueDefault;
		this.deleteTrueDefault = deleteTrueDefault;
		this.executeTrueDefault = executeTrueDefault;
		this.createPropertyTrueDefault = createPropertyTrueDefault;
		this.readPropertyTrueDefault = readPropertyTrueDefault;
		this.updatePropertyTrueDefault = updatePropertyTrueDefault;
		this.deletePropertyTrueDefault = deletePropertyTrueDefault;
		spes = new ScopedEntityPermissionEvaluation[scopes.length];
	}

	public HashMap<String, EntityPropertyPermissionEvaluation> getPropertyPermissions()
	{
		return propertyPermissions;
	}

	public ScopedEntityPermissionEvaluation[] getSpes()
	{
		return spes;
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
		for (ScopedEntityPermissionEvaluation spe : spes)
		{
			if (spe != null)
			{
				spe.reset();
			}
		}
		create = null;
		read = null;
		update = null;
		delete = null;
		execute = null;
		for (Entry<String, EntityPropertyPermissionEvaluation> entry : propertyPermissions)
		{
			EntityPropertyPermissionEvaluation propertyPermission = entry.getValue();
			propertyPermission.reset();
			unusedPropertyPermissions.add(propertyPermission);
		}
		propertyPermissions.clear();
	}

	@Override
	public IScopedEntityPermissionEvaluation scope(ISecurityScope scope)
	{
		for (int a = scopes.length; a-- > 0;)
		{
			if (scopes[a] == scope)
			{
				ScopedEntityPermissionEvaluation spe = spes[a];
				if (spe == null)
				{
					spe = new ScopedEntityPermissionEvaluation(this);
					spes[a] = spe;
				}
				return spe;
			}
		}
		throw new IllegalArgumentException(ISecurityScope.class.getSimpleName() + " not known: " + scope + ". Known are: " + Arrays.toString(scopes));
	}

	@Override
	public IExecuteEntityStep allowCUD()
	{
		return allowCreate().allowUpdate().allowDelete();
	}

	@Override
	public IExecuteEntityStep skipCUD()
	{
		return skipCreate().skipUpdate().skipDelete();
	}

	@Override
	public IExecuteEntityStep denyCUD()
	{
		return denyCreate().denyUpdate().denyDelete();
	}

	@Override
	public IUpdateEntityStep allowCreate()
	{
		if (create == null || !createTrueDefault)
		{
			create = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IUpdateEntityStep skipCreate()
	{
		return this;
	}

	@Override
	public IUpdateEntityStep denyCreate()
	{
		if (create == null || createTrueDefault)
		{
			create = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public ICreateEntityStep allowRead()
	{
		if (read == null || !readTrueDefault)
		{
			read = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public void denyRead()
	{
		if (read == null || readTrueDefault)
		{
			read = Boolean.FALSE;
		}
		denyCreate();
		denyUpdate();
		denyDelete();
		denyExecute();
	}

	@Override
	public IDeleteEntityStep allowUpdate()
	{
		if (update == null || !updateTrueDefault)
		{
			update = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IDeleteEntityStep skipUpdate()
	{
		return this;
	}

	@Override
	public IDeleteEntityStep denyUpdate()
	{
		if (update == null || updateTrueDefault)
		{
			update = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IExecuteEntityStep allowDelete()
	{
		if (delete == null || !deleteTrueDefault)
		{
			delete = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IExecuteEntityStep skipDelete()
	{
		return this;
	}

	@Override
	public IExecuteEntityStep denyDelete()
	{
		if (delete == null || deleteTrueDefault)
		{
			delete = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public void allowExecute()
	{
		if (execute == null || !executeTrueDefault)
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
		if (execute == null || executeTrueDefault)
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
	public ICreateEntityPropertyStep allowReadProperty(String propertyName)
	{
		// to read a single property it implies general read permission
		allowRead();
		EntityPropertyPermissionEvaluation propertyPermission = getPropertyPermission(propertyName);
		propertyPermission.allowReadProperty();
		return propertyPermission;
	}

	@Override
	public IEntityPermissionEvaluation denyReadProperty(String propertyName)
	{
		EntityPropertyPermissionEvaluation propertyPermission = getPropertyPermission(propertyName);
		propertyPermission.denyReadProperty();
		return this;
	}

	protected EntityPropertyPermissionEvaluation getPropertyPermission(String propertyName)
	{
		EntityPropertyPermissionEvaluation propertyPermission = propertyPermissions.get(propertyName);
		if (propertyPermission != null)
		{
			return propertyPermission;
		}
		propertyPermission = unusedPropertyPermissions.popLastElement();
		if (propertyPermission == null)
		{
			propertyPermission = new EntityPropertyPermissionEvaluation(createTrueDefault, readTrueDefault, updateTrueDefault, deleteTrueDefault);
		}
		propertyPermissions.put(propertyName, propertyPermission);
		return propertyPermission;
	}

	public void applyTypePropertyPrivilege(String propertyName, ITypePropertyPrivilege propertyPrivilege)
	{
		if (Boolean.FALSE.equals(propertyPrivilege.isReadAllowed()))
		{
			denyReadProperty(propertyName);
			return;
		}
		ICreateEntityPropertyStep createStep = allowReadProperty(propertyName);
		Boolean createAllowed = propertyPrivilege.isCreateAllowed();
		Boolean updateAllowed = propertyPrivilege.isUpdateAllowed();
		Boolean deleteAllowed = propertyPrivilege.isDeleteAllowed();

		IUpdateEntityPropertyStep updateStep = createAllowed != null ? (createAllowed.booleanValue() ? createStep.allowCreateProperty() : createStep
				.denyCreateProperty()) : createStep.skipCreateProperty();
		IDeleteEntityPropertyStep deleteStep = updateAllowed != null ? (updateAllowed.booleanValue() ? updateStep.allowUpdateProperty() : updateStep
				.denyUpdateProperty()) : updateStep.skipUpdateProperty();
		if (deleteAllowed == null)
		{
			deleteStep.skipDeleteProperty();
		}
		else if (deleteAllowed.booleanValue())
		{
			deleteStep.allowDeleteProperty();
		}
		else
		{
			deleteStep.denyDeleteProperty();
		}

	}
}
