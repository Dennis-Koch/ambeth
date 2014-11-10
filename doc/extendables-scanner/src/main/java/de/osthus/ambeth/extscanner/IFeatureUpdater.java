package de.osthus.ambeth.extscanner;

import de.osthus.ambeth.collections.IMap;
import de.osthus.classbrowser.java.TypeDescription;

public interface IFeatureUpdater
{
	IMap<String, FeatureEntry> buildFeatureMap(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes);
}