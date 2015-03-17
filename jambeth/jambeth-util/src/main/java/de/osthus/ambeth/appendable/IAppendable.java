package de.osthus.ambeth.appendable;

public interface IAppendable
{
	IAppendable append(CharSequence value);

	IAppendable append(char value);
}
