package com.koch.ambeth.security.server.privilege.evaluation;

public interface ICreateEntityPropertyStep
{
	IUpdateEntityPropertyStep allowCreateProperty();

	void allowCUDProperty();

	IUpdateEntityPropertyStep skipCreateProperty();

	void skipCUDProperty();

	IUpdateEntityPropertyStep denyCreateProperty();

	void denyCUDProperty();
}
