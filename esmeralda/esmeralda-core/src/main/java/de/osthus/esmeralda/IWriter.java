package de.osthus.esmeralda;

public interface IWriter
{
	IWriter append(char c);

	IWriter append(CharSequence csq);

	IWriter append(CharSequence csq, int start, int end);
}
