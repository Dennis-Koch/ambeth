package com.koch.ambeth.xml;

public interface INameBasedHandler
{
	Object readObject(Class<?> returnType, String elementName, int id, IReader reader);

	boolean writesCustom(Object obj, Class<?> type, IWriter writer);
}
