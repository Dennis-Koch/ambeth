package de.osthus.esmeralda;

public interface IToDoWriter
{
	void clearToDoFolder();

	void write(String topic, String todo);
}
