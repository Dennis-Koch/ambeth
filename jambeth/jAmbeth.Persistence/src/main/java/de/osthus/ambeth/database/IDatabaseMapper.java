package de.osthus.ambeth.database;

import de.osthus.ambeth.persistence.IDatabase;

public interface IDatabaseMapper
{
	void mapFields(IDatabase database);

	void mapLinks(IDatabase database);
}
