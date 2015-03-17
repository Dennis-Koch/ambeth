package de.osthus.esmeralda.misc;

public interface IWriter
{
	IWriter append(char c);

	IWriter append(CharSequence csq);

	IWriter append(CharSequence csq, int start, int end);
}
