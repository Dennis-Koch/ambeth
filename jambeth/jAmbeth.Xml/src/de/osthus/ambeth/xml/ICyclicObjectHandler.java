package de.osthus.ambeth.xml;

public interface ICyclicObjectHandler
{
	Object readObject(IReader reader);

	Object readObject(Class<?> type, int id, IReader reader);
}
