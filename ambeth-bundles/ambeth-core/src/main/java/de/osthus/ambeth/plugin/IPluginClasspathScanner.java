package de.osthus.ambeth.plugin;

import java.util.List;

public interface IPluginClasspathScanner
{
	List<Class<?>> scanClassesAnnotatedWith(Class<?>... annotationTypes);
}
