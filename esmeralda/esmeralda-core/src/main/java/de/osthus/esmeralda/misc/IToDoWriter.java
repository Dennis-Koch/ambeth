package de.osthus.esmeralda.misc;

public interface IToDoWriter
{
	void clearToDoFolder(String languagePathName);

	void write(String topic, String todo);
}
