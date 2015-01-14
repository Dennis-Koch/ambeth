package de.osthus.esmeralda.misc;

public interface IToDoWriter
{
	void clearToDoFolder(String languagePathName);

	void write(String topic, String todo);

	void write(String topic, String todo, String languagePathName, boolean dryRun);
}
