package de.osthus.ambeth.privilege.evaluation;

public interface IDeleteEntityPropertyStep
{
	void allowDeleteProperty();

	void skipDeleteProperty();

	void denyDeleteProperty();
}
