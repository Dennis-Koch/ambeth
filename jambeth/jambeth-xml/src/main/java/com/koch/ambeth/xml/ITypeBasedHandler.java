package com.koch.ambeth.xml;

public interface ITypeBasedHandler
{
	Object readObject(Class<?> returnType, Class<?> objType, int id, IReader reader);

	void writeObject(Object obj, Class<?> type, IWriter writer);
}
