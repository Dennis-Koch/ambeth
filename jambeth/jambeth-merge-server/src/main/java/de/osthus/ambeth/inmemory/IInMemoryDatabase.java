package de.osthus.ambeth.inmemory;

import java.util.Collection;

public interface IInMemoryDatabase
{
	void initialSetup(Collection<?> entities);
}