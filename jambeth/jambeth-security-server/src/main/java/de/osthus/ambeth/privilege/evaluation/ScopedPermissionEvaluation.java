package de.osthus.ambeth.privilege.evaluation;

public class ScopedPermissionEvaluation implements IScopedPermissionEvaluation, IScopedPermissionEvaluationReadStep, IScopedPermissionEvaluationUpdateStep,
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
	public IScopedPermissionEvaluationReadStep allowCreate()
	{
		create = Boolean.TRUE;
		return this;
	}

	@Override
	public IScopedPermissionEvaluationReadStep skipCreate()
	{
		return this;
	}

	@Override
	public IScopedPermissionEvaluationReadStep denyCreate()
	{
		create = Boolean.FALSE;
		return this;
	}

	@Override
	public IScopedPermissionEvaluationUpdateStep allowRead()
	{
		read = Boolean.TRUE;
		return this;
	}

	@Override
	public IScopedPermissionEvaluationUpdateStep skipRead()
	{
		return this;
	}

	@Override
	public IScopedPermissionEvaluationUpdateStep denyRead()
	{
		read = Boolean.FALSE;
		return this;
	}

	@Override
	public IScopedPermissionEvaluationDeleteStep allowUpdate()
	{
		update = Boolean.TRUE;
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
		update = Boolean.FALSE;
		return this;
	}

	@Override
	public IScopedPermissionEvaluationExecuteStep allowDelete()
	{
		delete = Boolean.TRUE;
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
		delete = Boolean.FALSE;
		return this;
	}

	@Override
	public IPermissionEvaluationResult allowExecute()
	{
		execute = Boolean.TRUE;
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
		execute = Boolean.FALSE;
		return permissionEvaluation;
	}

	@Override
	public IPermissionEvaluationResult allowEach()
	{
		return allowCreate().allowRead().allowUpdate().allowDelete().allowExecute();
	}

	@Override
	public IPermissionEvaluationResult skipEach()
	{
		return skipCreate().skipRead().skipUpdate().skipDelete().skipExecute();
	}

	@Override
	public IPermissionEvaluationResult denyEach()
	{
		return denyCreate().denyRead().denyUpdate().denyDelete().denyExecute();
	}
}
