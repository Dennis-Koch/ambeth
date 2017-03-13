package com.koch.ambeth.security.server.privilege.evaluation;

public interface IDeleteEntityPropertyStep
{
	void allowDeleteProperty();

	void skipDeleteProperty();

	void denyDeleteProperty();
}
