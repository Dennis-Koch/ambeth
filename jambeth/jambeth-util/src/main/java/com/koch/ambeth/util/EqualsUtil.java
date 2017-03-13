package com.koch.ambeth.util;

public final class EqualsUtil
{
	protected EqualsUtil()
	{
		// Intended blank
	}

	public static boolean equals(Object left, Object right)
	{
		if (left == null)
		{
			return right == null;
		}
		return left.equals(right);
	}
}
