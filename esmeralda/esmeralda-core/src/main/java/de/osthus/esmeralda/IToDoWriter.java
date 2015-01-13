package de.osthus.esmeralda;

public interface IToDoWriter
{
	void clearToDoFolder(String languagePathName);

	void write(String topic, String todo);
}
