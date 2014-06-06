package de.osthus.ambeth.privilege.evaluation;

public interface IPermissionEvaluationUpdateStep
{
	IPermissionEvaluationDeleteStep allowUpdate();

	IPermissionEvaluationDeleteStep skipUpdate();

	IPermissionEvaluationDeleteStep denyUpdate();
}
