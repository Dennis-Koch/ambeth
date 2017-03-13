package com.koch.ambeth.merge.server.inmemory;

import java.util.Collection;

public interface IInMemoryDatabase
{
	void initialSetup(Collection<?> entities);
}