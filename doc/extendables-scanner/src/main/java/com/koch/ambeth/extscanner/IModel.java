package com.koch.ambeth.extscanner;

import com.koch.ambeth.extscanner.model.AnnotationEntry;
import com.koch.ambeth.extscanner.model.ConfigurationEntry;
import com.koch.ambeth.extscanner.model.ExtendableEntry;
import com.koch.ambeth.extscanner.model.FeatureEntry;
import com.koch.ambeth.extscanner.model.ModuleEntry;
import com.koch.ambeth.extscanner.model.TypeEntry;
import com.koch.classbrowser.java.TypeDescription;

public interface IModel {
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
