package de.osthus.ambeth.extscanner;

import de.osthus.ambeth.collections.IMap;
import de.osthus.classbrowser.java.TypeDescription;

public interface IConfigurationUpdater
{
	IMap<String, ConfigurationEntry> buildConfigurationMap(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes, IMap<String, ModuleEntry> nameToModuleMap);
}