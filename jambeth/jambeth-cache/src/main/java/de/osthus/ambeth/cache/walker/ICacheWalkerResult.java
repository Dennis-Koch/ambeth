package de.osthus.ambeth.cache.walker;

import de.osthus.ambeth.util.IPrintable;

public interface ICacheWalkerResult extends IPrintable
{
	void toString(StringBuilder sb, int tabCount);
}