package com.koch.ambeth.persistence.init;

import com.koch.ambeth.persistence.api.IDatabase;

public interface IDatabaseInit<D extends IDatabase>
{

	void init(D database);

}
