package com.koch.ambeth.cache.walker;

import com.koch.ambeth.util.IPrintable;

public interface ICacheWalkerResult extends IPrintable
{
	void toString(StringBuilder sb, int tabCount);
}