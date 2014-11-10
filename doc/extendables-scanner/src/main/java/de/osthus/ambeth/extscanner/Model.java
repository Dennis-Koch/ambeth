package de.osthus.ambeth.extscanner;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.TypeDescription;

public class Model implements IModel
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final HashMap<String, ModuleEntry> nameToModuleMap = new HashMap<String, ModuleEntry>();

	protected final HashMap<String, ExtendableEntry> nameToExtendableMap = new HashMap<String, ExtendableEntry>();

	protected final HashMap<String, FeatureEntry> nameToFeatureMap = new HashMap<String, FeatureEntry>();

	protected final HashMap<String, ConfigurationEntry> nameToConfigurationMap = new HashMap<String, ConfigurationEntry>();

	protected final HashMap<String, TypeEntry> nameToTypeMap = new HashMap<String, TypeEntry>();

	@Override
	public Iterable<ModuleEntry> allModules()
	{
		return nameToModuleMap.values();
	}

	@Override
	public Iterable<ConfigurationEntry> allConfigurations()
	{
		return nameToConfigurationMap.values();
	}

	@Override
	public Iterable<ExtendableEntry> allExtendables()
	{
		return nameToExtendableMap.values();
	}

	@Override
	public Iterable<FeatureEntry> allFeatures()
	{
		return nameToFeatureMap.values();
	}

	@Override
	public void addModule(String moduleName, ModuleEntry moduleEntry)
	{
		if (nameToModuleMap.putIfNotExists(moduleName, moduleEntry))
		{
			return;
		}
		throw new IllegalStateException("Module name not unique: " + moduleEntry.moduleTexFile + " vs. " + nameToModuleMap.get(moduleName).moduleTexFile);
	}

	@Override
	public void addConfiguration(String configurationName, ConfigurationEntry configurationEntry)
	{
		if (nameToConfigurationMap.putIfNotExists(configurationName, configurationEntry))
		{
			return;
		}
		throw new IllegalStateException("Configuration name not unique: " + configurationName);
	}

	@Override
	public void addExtendable(String extendableName, ExtendableEntry extendableEntry)
	{
		if (nameToExtendableMap.putIfNotExists(extendableName, extendableEntry))
		{
			return;
		}
		throw new IllegalStateException("Extendable name not unique: " + extendableName);
	}

	@Override
	public void addFeature(String featureName, FeatureEntry featureEntry)
	{
		if (nameToFeatureMap.putIfNotExists(featureName, featureEntry))
		{
			return;
		}
		throw new IllegalStateException("Feature name not unique: " + featureEntry.featureTexFile + " vs. " + nameToFeatureMap.get(featureName).featureTexFile);
	}

	@Override
	public FeatureEntry resolveFeature(String featureName)
	{
		return nameToFeatureMap.get(featureName);
	}

	@Override
	public ModuleEntry resolveModule(String moduleName)
	{
		moduleName = moduleName.toLowerCase();
		ModuleEntry moduleEntry = nameToModuleMap.get(moduleName);
		if (moduleEntry == null && moduleName.startsWith("jambeth"))
		{
			moduleName = moduleName.substring("jambeth".length());
			moduleEntry = nameToModuleMap.get(moduleName);
		}
		if (moduleEntry == null && moduleName.startsWith("ambeth"))
		{
			moduleName = moduleName.substring("ambeth".length());
			moduleEntry = nameToModuleMap.get(moduleName);
		}
		if (moduleEntry == null && (moduleName.startsWith(".") || moduleName.startsWith("-")))
		{
			moduleName = moduleName.substring(1);
			moduleEntry = nameToModuleMap.get(moduleName);
		}
		if (moduleEntry == null && moduleName.contains("-"))
		{
			int indexOf = moduleName.indexOf('-');
			moduleName = moduleName.substring(0, indexOf);
			moduleEntry = nameToModuleMap.get(moduleName);
		}
		return moduleEntry;
	}

	@Override
	public TypeEntry resolveTypeEntry(TypeDescription typeDesc)
	{
		TypeEntry typeEntry = nameToTypeMap.get(typeDesc.getFullTypeName());
		if (typeEntry == null)
		{
			typeEntry = new TypeEntry(typeDesc);
			nameToTypeMap.put(typeDesc.getFullTypeName(), typeEntry);
		}
		return typeEntry;
	}

	@Override
	public ConfigurationEntry resolveConfiguration(String configurationName)
	{
		return nameToConfigurationMap.get(configurationName);
	}

	@Override
	public ExtendableEntry resolveExtendable(String extendableName)
	{
		return nameToExtendableMap.get(extendableName);
	}
}
