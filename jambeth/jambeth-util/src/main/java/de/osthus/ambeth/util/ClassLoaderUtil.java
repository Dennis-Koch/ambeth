package de.osthus.ambeth.util;


public final class ClassLoaderUtil
{
	public static boolean isParentOf(ClassLoader potentialParentCL, ClassLoader potentialChildCL)
	{
		if (potentialParentCL == null || potentialChildCL == null)
		{
			return false;
		}
		ClassLoader childCL = potentialChildCL;
		while (childCL != null)
		{
			if (childCL.equals(potentialParentCL))
			{
				return true;
			}
			childCL = childCL.getParent();
		}
		return false;
	}

	private ClassLoaderUtil()
	{
		// Intended blank
	}
}
