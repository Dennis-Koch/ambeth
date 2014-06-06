package de.osthus.ambeth.privilege.evaluation;


public class ScopedPermissionEvaluation implements IScopedPermissionEvaluation, IScopedPermissionEvaluationReadStep, IScopedPermissionEvaluationUpdateStep,
		IScopedPermissionEvaluationDeleteStep
{
	protected Boolean create, read, update, delete;

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
	public void allowDelete()
	{
		delete = Boolean.TRUE;
	}

	@Override
	public void skipDelete()
	{
		// intended blank
	}

	@Override
	public void denyDelete()
	{
		delete = Boolean.FALSE;
	}

	@Override
	public void allowEach()
	{
		allowCreate().allowRead().allowUpdate().allowDelete();
	}

	@Override
	public void skipEach()
	{
		// intended blank
	}

	@Override
	public void denyEach()
	{
		denyCreate().denyRead().denyUpdate().denyDelete();
	}
}
