package de.osthus.ambeth.xml;

public interface ICyclicXmlController
{
	Object readObject(IReader reader);

	Object readObject(Class<?> returnType, IReader reader);
	
	void writeObject(Object obj, IWriter writer);
}