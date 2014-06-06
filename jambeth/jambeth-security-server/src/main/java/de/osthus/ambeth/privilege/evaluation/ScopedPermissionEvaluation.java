package de.osthus.ambeth.privilege.evaluation;

public class ScopedPermissionEvaluation implements IScopedPermissionEvaluation, IScopedPermissionEvaluationReadStep, IScopedPermissionEvaluationUpdateStep,
		IScopedPermissionEvaluationDeleteStep
{
	protected Boolean create, read, update, delete;

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

	public void reset()
	{
		create = null;
		read = null;
		update = null;
		delete = null;
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
	public IPermissionEvaluationResult allowDelete()
	{
		delete = Boolean.TRUE;
		return permissionEvaluation;
	}

	@Override
	public IPermissionEvaluationResult skipDelete()
	{
		return permissionEvaluation;
	}

	@Override
	public IPermissionEvaluationResult denyDelete()
	{
		delete = Boolean.FALSE;
		return permissionEvaluation;
	}

	@Override
	public IPermissionEvaluationResult allowEach()
	{
		return allowCreate().allowRead().allowUpdate().allowDelete();
	}

	@Override
	public IPermissionEvaluationResult skipEach()
	{
		return skipCreate().skipRead().skipUpdate().skipDelete();
	}

	@Override
	public IPermissionEvaluationResult denyEach()
	{
		return denyCreate().denyRead().denyUpdate().denyDelete();
	}
}
