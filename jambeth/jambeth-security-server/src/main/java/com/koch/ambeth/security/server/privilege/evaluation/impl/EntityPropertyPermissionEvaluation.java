package com.koch.ambeth.security.server.privilege.evaluation.impl;

import com.koch.ambeth.security.server.privilege.evaluation.ICreateEntityPropertyStep;
import com.koch.ambeth.security.server.privilege.evaluation.IDeleteEntityPropertyStep;
import com.koch.ambeth.security.server.privilege.evaluation.IUpdateEntityPropertyStep;

public class EntityPropertyPermissionEvaluation implements ICreateEntityPropertyStep, IUpdateEntityPropertyStep, IDeleteEntityPropertyStep
{
	protected Boolean create, read, update, delete;

	protected final boolean createTrueDefault, readTrueDefault, updateTrueDefault, deleteTrueDefault;

	public EntityPropertyPermissionEvaluation(boolean createTrueDefault, boolean readTrueDefault, boolean updateTrueDefault, boolean deleteTrueDefault)
	{
		this.createTrueDefault = createTrueDefault;
		this.readTrueDefault = readTrueDefault;
		this.updateTrueDefault = updateTrueDefault;
		this.deleteTrueDefault = deleteTrueDefault;
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

	public void reset()
	{
		create = null;
		read = null;
		update = null;
		delete = null;
	}

	public void allowReadProperty()
	{
		if (read == null || !readTrueDefault)
		{
			read = Boolean.TRUE;
		}
	}

	public void denyReadProperty()
	{
		if (read == null || readTrueDefault)
		{
			read = Boolean.FALSE;
		}
		denyCreateProperty();
		denyUpdateProperty();
		denyDeleteProperty();
	}

	@Override
	public void allowDeleteProperty()
	{
		if (delete == null || !deleteTrueDefault)
		{
			delete = Boolean.TRUE;
		}
	}

	@Override
	public void skipDeleteProperty()
	{
		// intended blank
	}

	@Override
	public void denyDeleteProperty()
	{
		if (delete == null || deleteTrueDefault)
		{
			delete = Boolean.FALSE;
		}
	}

	@Override
	public IDeleteEntityPropertyStep allowUpdateProperty()
	{
		if (update == null || !updateTrueDefault)
		{
			update = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IDeleteEntityPropertyStep skipUpdateProperty()
	{
		return this;
	}

	@Override
	public IDeleteEntityPropertyStep denyUpdateProperty()
	{
		if (update == null || updateTrueDefault)
		{
			update = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IUpdateEntityPropertyStep allowCreateProperty()
	{
		if (create == null || !createTrueDefault)
		{
			create = Boolean.TRUE;
		}

		return this;
	}

	@Override
	public IUpdateEntityPropertyStep skipCreateProperty()
	{
		return this;
	}

	@Override
	public IUpdateEntityPropertyStep denyCreateProperty()
	{
		if (create == null || createTrueDefault)
		{
			create = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public void allowCUDProperty()
	{
		allowCreateProperty().allowUpdateProperty().allowDeleteProperty();
	}

	@Override
	public void skipCUDProperty()
	{
		skipCreateProperty().skipUpdateProperty().skipDeleteProperty();
	}

	@Override
	public void denyCUDProperty()
	{
		denyCreateProperty().denyUpdateProperty().denyDeleteProperty();
	}
}
