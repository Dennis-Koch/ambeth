package de.osthus.ambeth.init;

import de.osthus.ambeth.persistence.IDatabase;

public interface IDatabaseInit<D extends IDatabase>
{

	void init(D database);

}
