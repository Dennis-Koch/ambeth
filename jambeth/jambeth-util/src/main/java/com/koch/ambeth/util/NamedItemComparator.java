package com.koch.ambeth.util;

import java.io.Serializable;
import java.util.Comparator;

public class NamedItemComparator implements Comparator<INamed>, Serializable
{
	private static final long serialVersionUID = 1682312447151668687L;

	@Override
	public int compare(INamed leftItem, INamed rightItem)
	{
		return leftItem.getName().compareToIgnoreCase(rightItem.getName());
	}
}
