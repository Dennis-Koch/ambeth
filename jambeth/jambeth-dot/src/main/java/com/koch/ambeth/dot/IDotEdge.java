package com.koch.ambeth.dot;

public interface IDotEdge
{
	IDotEdge attribute(String key, String value);

	IDotWriter endEdge();
}