package com.koch.ambeth.extscanner;

import java.util.Map.Entry;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.classbrowser.java.TypeDescription;

public class Model implements IModel {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final HashMap<String, ModuleEntry> nameToModuleMap = new HashMap<>();

	protected final HashMap<String, ExtendableEntry> nameToExtendableMap = new HashMap<>();

	protected final HashMap<String, FeatureEntry> nameToFeatureMap = new HashMap<>();

	protected final HashMap<String, ConfigurationEntry> nameToConfigurationMap = new HashMap<>();

	protected final HashMap<String, AnnotationEntry> nameToAnnotationMap = new HashMap<>();

	protected final HashMap<String, TypeEntry> nameToTypeMap = new HashMap<>();

	@Override
	public Iterable<ModuleEntry> allModules() {
		return nameToModuleMap.values();
	}

	@Override
	public Iterable<ConfigurationEntry> allConfigurations() {
		return nameToConfigurationMap.values();
	}

	@Override
	public Iterable<ExtendableEntry> allExtendables() {
		return nameToExtendableMap.values();
	}

	@Override
	public Iterable<FeatureEntry> allFeatures() {
		return nameToFeatureMap.values();
	}

	@Override
	public Iterable<AnnotationEntry> allAnnotations() {
		return nameToAnnotationMap.values();
	}

	@Override
	public void addModule(String moduleName, ModuleEntry moduleEntry) {
		if (nameToModuleMap.putIfNotExists(moduleName, moduleEntry)) {
			return;
		}
		throw new IllegalStateException("Module name not unique: " + moduleEntry.moduleTexFile + " vs. "
				+ nameToModuleMap.get(moduleName).moduleTexFile);
	}

	@Override
	public void addConfiguration(String configurationName, ConfigurationEntry configurationEntry) {
		if (nameToConfigurationMap.putIfNotExists(configurationName, configurationEntry)) {
			return;
		}
		throw new IllegalStateException("Configuration name not unique: " + configurationName);
	}

	@Override
	public void addExtendable(String extendableName, ExtendableEntry extendableEntry) {
		if (nameToExtendableMap.putIfNotExists(extendableName, extendableEntry)) {
			return;
		}
		throw new IllegalStateException("Extendable name not unique: " + extendableName);
	}

	@Override
	public void addFeature(String featureName, FeatureEntry featureEntry) {
		if (nameToFeatureMap.putIfNotExists(featureName, featureEntry)) {
			return;
		}
		throw new IllegalStateException("Feature name not unique: " + featureEntry.getTexFile()
				+ " vs. " + nameToFeatureMap.get(featureName).getTexFile());
	}

	@Override
	public void addAnnotation(String annotationName, AnnotationEntry annotationEntry) {
		if (nameToAnnotationMap.putIfNotExists(annotationName, annotationEntry)) {
			return;
		}
		throw new IllegalStateException("Annotation name not unique: " + annotationEntry.annotationName
				+ " vs. " + nameToAnnotationMap.get(annotationName).annotationName);
	}

	@Override
	public FeatureEntry resolveFeature(String featureName) {
		return nameToFeatureMap.get(featureName);
	}

	@Override
	public ModuleEntry resolveModule(String moduleName) {
		String moduleNameTry = moduleName.toLowerCase();
		ModuleEntry moduleEntry = nameToModuleMap.get(moduleNameTry);
		if (moduleEntry == null && moduleNameTry.startsWith("jambeth")) {
			moduleNameTry = moduleNameTry.substring("jambeth".length());
			moduleEntry = nameToModuleMap.get(moduleNameTry);
		}
		if (moduleEntry == null && moduleNameTry.startsWith("ambeth")) {
			moduleNameTry = moduleNameTry.substring("ambeth".length());
			moduleEntry = nameToModuleMap.get(moduleNameTry);
		}
		if (moduleEntry == null && (moduleNameTry.startsWith(".") || moduleNameTry.startsWith("-"))) {
			moduleNameTry = moduleNameTry.substring(1);
			moduleEntry = nameToModuleMap.get(moduleNameTry);
		}
		if (moduleEntry == null && moduleNameTry.contains("-")) {
			int indexOf = moduleNameTry.indexOf('-');
			moduleNameTry = moduleNameTry.substring(0, indexOf);
			moduleEntry = nameToModuleMap.get(moduleNameTry);
		}
		if (moduleEntry != null) {
			moduleEntry.mavenModules.add(moduleName);
		}
		return moduleEntry;
	}

	@Override
	public TypeEntry resolveTypeEntry(TypeDescription typeDesc) {
		TypeEntry typeEntry = nameToTypeMap.get(typeDesc.getFullTypeName());
		if (typeEntry == null) {
			typeEntry = new TypeEntry(typeDesc);
			nameToTypeMap.put(typeDesc.getFullTypeName(), typeEntry);
		}
		return typeEntry;
	}

	@Override
	public ConfigurationEntry resolveConfiguration(String configurationName) {
		return nameToConfigurationMap.get(configurationName);
	}

	@Override
	public ExtendableEntry resolveExtendable(String extendableName) {
		return nameToExtendableMap.get(extendableName);
	}

	@Override
	public AnnotationEntry resolveAnnotation(String annotationName) {
		return nameToAnnotationMap.get(annotationName);
	}

	@Override
	public FeatureEntry[] resolveFeaturesByType(TypeEntry typeEntry) {
		ArrayList<FeatureEntry> featureEntries = new ArrayList<>();

		for (Entry<String, FeatureEntry> entry : nameToFeatureMap) {
			FeatureEntry featureEntry = entry.getValue();
			if (!featureEntry.typeConditions.contains(typeEntry.typeDesc.getName())
					&& !featureEntry.typeConditions.contains(typeEntry.typeDesc.getFullTypeName())) {
				continue;
			}
			featureEntries.add(featureEntry);
		}
		// String name = typeDescr.getName();
		// FeatureEntry featureEntry = model.resolveFeature("I" + name);
		// if (featureEntry == null)
		// {
		// featureEntry = model.resolveFeature(name);
		// }
		// if (featureEntry == null && name.startsWith("I"))
		// {
		// featureEntry = model.resolveFeature(name.substring(1));
		// }
		// return featureEntry;
		//
		// return null;

		return featureEntries.toArray(FeatureEntry.class);
	}
}
