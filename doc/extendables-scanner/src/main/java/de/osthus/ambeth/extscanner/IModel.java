package de.osthus.ambeth.extscanner;

import de.osthus.classbrowser.java.TypeDescription;

public interface IModel
{
	ModuleEntry resolveModule(String moduleName);

	ConfigurationEntry resolveConfiguration(String configurationName);

	void addConfiguration(String configurationName, ConfigurationEntry configurationEntry);

	void addModule(String moduleName, ModuleEntry moduleEntry);

	Iterable<ModuleEntry> allModules();

	Iterable<ConfigurationEntry> allConfigurations();

	Iterable<FeatureEntry> allFeatures();

	Iterable<ExtendableEntry> allExtendables();

	Iterable<AnnotationEntry> allAnnotations();

	TypeEntry resolveTypeEntry(TypeDescription typeDescr);

	void addFeature(String featureName, FeatureEntry featureEntry);

	FeatureEntry resolveFeature(String featureName);

	ExtendableEntry resolveExtendable(String extendableName);

	void addExtendable(String extendableName, ExtendableEntry extendableEntry);

	AnnotationEntry resolveAnnotation(String annotationName);

	void addAnnotation(String annotationName, AnnotationEntry annotationEntry);

	FeatureEntry[] resolveFeaturesByType(TypeEntry typeEntry);
}
