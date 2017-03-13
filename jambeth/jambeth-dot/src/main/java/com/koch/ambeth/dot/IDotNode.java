package com.koch.ambeth.dot;

public interface IDotNode
{
	IDotNode attribute(String key, String value);

	IDotWriter endNode();
}