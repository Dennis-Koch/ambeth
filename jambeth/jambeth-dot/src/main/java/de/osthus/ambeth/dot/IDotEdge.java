package de.osthus.ambeth.dot;

public interface IDotEdge
{
	IDotEdge attribute(String key, String value);

	IDotWriter endEdge();
}