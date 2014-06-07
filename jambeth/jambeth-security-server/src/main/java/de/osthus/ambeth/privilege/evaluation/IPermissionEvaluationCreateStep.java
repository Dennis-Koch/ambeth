package de.osthus.ambeth.privilege.evaluation;

public interface IPermissionEvaluationCreateStep
{
	IPermissionEvaluationUpdateStep allowCreate();

	IPermissionEvaluationUpdateStep skipCreate();

	IPermissionEvaluationUpdateStep denyCreate();
}
