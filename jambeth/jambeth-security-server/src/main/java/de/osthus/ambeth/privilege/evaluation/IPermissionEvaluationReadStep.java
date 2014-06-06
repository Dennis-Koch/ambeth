package de.osthus.ambeth.privilege.evaluation;

public interface IPermissionEvaluationReadStep
{
	IPermissionEvaluationUpdateStep allowRead();

	IPermissionEvaluationUpdateStep skipRead();

	IPermissionEvaluationUpdateStep denyRead();
}
