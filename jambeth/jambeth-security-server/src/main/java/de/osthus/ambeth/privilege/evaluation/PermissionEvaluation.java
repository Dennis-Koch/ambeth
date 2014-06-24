package de.osthus.ambeth.privilege.evaluation;

import java.util.Arrays;

import de.osthus.ambeth.model.ISecurityScope;

public class PermissionEvaluation implements IPermissionEvaluation, IPermissionEvaluationCreateStep, IPermissionEvaluationUpdateStep,
		IPermissionEvaluationDeleteStep, IPermissionEvaluationExecuteStep, IPermissionEvaluationResult
{
	protected final ISecurityScope[] scopes;

	protected final ScopedPermissionEvaluation[] spes;

	protected Boolean create, read, update, delete, execute;

	protected boolean createTrueDefault, readTrueDefault, updateTrueDefault, deleteTrueDefault, executeTrueDefault;

	public PermissionEvaluation(ISecurityScope[] scopes, boolean createTrueDefault, boolean readTrueDefault, boolean updateTrueDefault,
			boolean deleteTrueDefault, boolean executeTrueDefault)
	{
		this.scopes = scopes;
		this.createTrueDefault = createTrueDefault;
		this.readTrueDefault = readTrueDefault;
		this.updateTrueDefault = updateTrueDefault;
		this.deleteTrueDefault = deleteTrueDefault;
		this.executeTrueDefault = executeTrueDefault;
		spes = new ScopedPermissionEvaluation[scopes.length];
	}

	public ScopedPermissionEvaluation[] getSpes()
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
		for (ScopedPermissionEvaluation spe : spes)
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
	}

	@Override
	public IScopedPermissionEvaluation scope(ISecurityScope scope)
	{
		for (int a = scopes.length; a-- > 0;)
		{
			if (scopes[a] == scope)
			{
				ScopedPermissionEvaluation spe = spes[a];
				if (spe == null)
				{
					spe = new ScopedPermissionEvaluation(this);
					spes[a] = spe;
				}
				return spe;
			}
		}
		throw new IllegalArgumentException(ISecurityScope.class.getSimpleName() + " not known: " + scope + ". Known are: " + Arrays.toString(scopes));
	}

	@Override
	public IPermissionEvaluationUpdateStep allowCreate()
	{
		if (!createTrueDefault)
		{
			create = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationUpdateStep skipCreate()
	{
		return this;
	}

	@Override
	public IPermissionEvaluationUpdateStep denyCreate()
	{
		if (createTrueDefault)
		{
			create = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationCreateStep allowRead()
	{
		if (!readTrueDefault)
		{
			read = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationResult denyRead()
	{
		if (readTrueDefault)
		{
			read = Boolean.FALSE;
		}
		denyCreate();
		denyUpdate();
		denyDelete();
		return this;
	}

	@Override
	public IPermissionEvaluationDeleteStep allowUpdate()
	{
		if (!updateTrueDefault)
		{
			update = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationDeleteStep skipUpdate()
	{
		return this;
	}

	@Override
	public IPermissionEvaluationDeleteStep denyUpdate()
	{
		if (updateTrueDefault)
		{
			update = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationExecuteStep allowDelete()
	{
		if (!deleteTrueDefault)
		{
			delete = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationExecuteStep skipDelete()
	{
		return this;
	}

	@Override
	public IPermissionEvaluationExecuteStep denyDelete()
	{
		if (deleteTrueDefault)
		{
			delete = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationResult allowExecute()
	{
		if (!executeTrueDefault)
		{
			execute = Boolean.TRUE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationResult skipExecute()
	{
		return this;
	}

	@Override
	public IPermissionEvaluationResult denyExecute()
	{
		if (executeTrueDefault)
		{
			execute = Boolean.FALSE;
		}
		return this;
	}

	@Override
	public IPermissionEvaluationResult allowEach()
	{
		return allowRead().allowCreate().allowUpdate().allowDelete().allowExecute();
	}

	@Override
	public IPermissionEvaluationResult skipEach()
	{
		return this;
	}

	@Override
	public IPermissionEvaluationResult denyEach()
	{
		return denyRead();
	}
}
