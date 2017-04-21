package com.koch.classbrowser.java;

import java.util.Collection;

public interface IDeprecation
{
	public interface IDeprecationInstance
	{
		boolean isDeprecated(Collection<AnnotationInfo> annotationNames);
	}

	public static final IDeprecationInstance INSTANCE = new IDeprecationInstance()
	{

		@Override
		public boolean isDeprecated(Collection<AnnotationInfo> annotationInfo)
		{
			for (AnnotationInfo annotation : annotationInfo)
			{
				String type = annotation.getAnnotationType();
				if (type.equals("java.lang.Deprecated") || type.equals("System.ObsoleteAttribute"))
				{
					return true;
				}
			}
			return false;
		}

	};

	boolean isDeprecated();
}
