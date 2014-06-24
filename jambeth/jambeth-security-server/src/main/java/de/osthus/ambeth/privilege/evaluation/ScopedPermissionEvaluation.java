package de.osthus.ambeth.privilege.evaluation;

public class ScopedPermissionEvaluation implements IScopedPermissionEvaluation, IScopedPermissionEvaluationCreateStep, IScopedPermissionEvaluationUpdateStep,
		IScopedPermissionEvaluationDeleteStep, IScopedPermissionEvaluationExecuteStep
{
	protected Boolean create, read, update, delete, execute = null;

	protected final PermissionEvaluation permissionEvaluation;

	public ScopedPermissionEvaluation(PermissionEvaluation permissionEvaluation)
	{
		this.permissionEvaluation = permissionEvaluation;
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
	}

	@Override
	public IScopedPermissionEvaluationUpdateStep allowCreate()
	{
		if (!permissionEvaluation.createTrueDefault)
		{
			create = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IScopedPermissionEvaluationUpdateStep skipCreate()
	{
		return this;
	}

	@Override
	public IScopedPermissionEvaluationUpdateStep denyCreate()
	{
		if (permissionEvaluation.createTrueDefault)
		{
			create = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IScopedPermissionEvaluationCreateStep allowRead()
	{
		if (!permissionEvaluation.readTrueDefault)
		{
			read = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationResult denyRead()
	{
		if (permissionEvaluation.readTrueDefault)
		{
			read = Boolean.FALSE;
		}
		return permissionEvaluation;
	}

	@Override
	public IScopedPermissionEvaluationDeleteStep allowUpdate()
	{
		if (!permissionEvaluation.updateTrueDefault)
		{
			update = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IScopedPermissionEvaluationDeleteStep skipUpdate()
	{
		return this;
	}

	@Override
	public IScopedPermissionEvaluationDeleteStep denyUpdate()
	{
		if (permissionEvaluation.updateTrueDefault)
		{
			update = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IScopedPermissionEvaluationExecuteStep allowDelete()
	{
		if (!permissionEvaluation.deleteTrueDefault)
		{
			delete = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IScopedPermissionEvaluationExecuteStep skipDelete()
	{
		return this;
	}

	@Override
	public IScopedPermissionEvaluationExecuteStep denyDelete()
	{
		if (permissionEvaluation.deleteTrueDefault)
		{
			delete = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationResult allowExecute()
	{
		if (!permissionEvaluation.executeTrueDefault)
		{
			execute = Boolean.TRUE;
		}
		return permissionEvaluation;
	}

	@Override
	public IPermissionEvaluationResult skipExecute()
	{
		return permissionEvaluation;
	}

	@Override
	public IPermissionEvaluationResult denyExecute()
	{
		if (permissionEvaluation.executeTrueDefault)
		{
			execute = Boolean.FALSE;
		}
		return permissionEvaluation;
	}

	@Override
	public IPermissionEvaluationResult allowEach()
	{
		return allowRead().allowCreate().allowUpdate().allowDelete().allowExecute();
	}

	@Override
	public IPermissionEvaluationResult skipEach()
	{
		return permissionEvaluation;
	}

	@Override
	public IPermissionEvaluationResult denyEach()
	{
		return denyRead();
	}
}
