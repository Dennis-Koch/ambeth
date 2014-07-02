package de.osthus.ambeth.privilege.evaluation;

public interface ICreateEntityStep
{
	IUpdateEntityStep allowCreate();

	IExecuteEntityStep allowCUD();

	IUpdateEntityStep skipCreate();

	IExecuteEntityStep skipCUD();

	IUpdateEntityStep denyCreate();

	IExecuteEntityStep denyCUD();
}
