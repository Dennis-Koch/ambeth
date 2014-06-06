package de.osthus.ambeth.privilege.evaluation;

import java.util.Arrays;

import de.osthus.ambeth.model.ISecurityScope;

public class PermissionEvaluation implements IPermissionEvaluation, IPermissionEvaluationReadStep, IPermissionEvaluationUpdateStep,
		IPermissionEvaluationDeleteStep, IPermissionEvaluationResult
{
	protected final ISecurityScope[] scopes;

	protected final ScopedPermissionEvaluation[] spes;

	protected Boolean create, read, update, delete;

	public PermissionEvaluation(ISecurityScope[] scopes)
	{
		this.scopes = scopes;
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
	public IPermissionEvaluationReadStep allowCreate()
	{
		create = Boolean.TRUE;
		return this;
	}

	@Override
	public IPermissionEvaluationReadStep skipCreate()
	{
		return this;
	}

	@Override
	public IPermissionEvaluationReadStep denyCreate()
	{
		create = Boolean.FALSE;
		return this;
	}

	@Override
	public IPermissionEvaluationUpdateStep allowRead()
	{
		read = Boolean.TRUE;
		return this;
	}

	@Override
	public IPermissionEvaluationUpdateStep skipRead()
	{
		return this;
	}

	@Override
	public IPermissionEvaluationUpdateStep denyRead()
	{
		read = Boolean.FALSE;
		return this;
	}

	@Override
	public IPermissionEvaluationDeleteStep allowUpdate()
	{
		update = Boolean.TRUE;
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
		update = Boolean.FALSE;
		return this;
	}

	@Override
	public IPermissionEvaluationResult allowDelete()
	{
		delete = Boolean.TRUE;
		return this;
	}

	@Override
	public IPermissionEvaluationResult skipDelete()
	{
		return this;
	}

	@Override
	public IPermissionEvaluationResult denyDelete()
	{
		delete = Boolean.FALSE;
		return this;
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
