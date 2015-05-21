package de.osthus.ambeth.dot;

public interface IDotNode
{
	IDotNode attribute(String key, String value);

	IDotWriter endNode();
}