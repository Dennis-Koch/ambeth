package com.koch.ambeth.util;

import java.util.List;

public interface IClasspathScanner
{
	List<Class<?>> scanClassesAnnotatedWith(Class<?>... annotationTypes);

	List<Class<?>> scanClassesImplementing(Class<?>... superTypes);
}