package de.osthus.classbrowser.java;

import java.util.Collection;

public interface IDeprecation
{

	public interface IDeprecationInstance
	{

		boolean isDeprecated(Collection<String> annotationNames);

	}

	public static final IDeprecationInstance INSTANCE = new IDeprecationInstance()
	{

		@Override
		public boolean isDeprecated(Collection<String> annotationNames)
		{
			return annotationNames.contains("java.lang.Deprecated") || annotationNames.contains("System.ObsoleteAttribute");
		}

	};

	boolean isDeprecated();

}
