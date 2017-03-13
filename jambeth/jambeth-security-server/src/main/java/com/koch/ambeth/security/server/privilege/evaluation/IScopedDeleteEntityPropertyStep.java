package com.koch.ambeth.security.server.privilege.evaluation;

public interface IScopedDeleteEntityPropertyStep
{
	void allowDeleteProperty();

	void skipDeleteProperty();

	void denyDeleteProperty();
}
