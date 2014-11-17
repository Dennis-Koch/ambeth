package de.osthus.ambeth.extscanner;

import java.io.File;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;

public class FeatureEntry implements IMultiPlatformFeature, ITexFileAware
{
	public final HashSet<TypeEntry> javaSrc = new HashSet<TypeEntry>();

	public final HashSet<TypeEntry> javascriptSrc = new HashSet<TypeEntry>();

	public final HashSet<TypeEntry> csharpSrc = new HashSet<TypeEntry>();

	public final HashSet<String> typeConditions = new HashSet<String>();

	public String javaFile;

	public String csharpFile;

	public final String featureName;

	@Override
	public boolean inJava()
	{
		return javaSrc.size() > 0;
	}

	@Override
	public boolean inJavascript()
	{
		return javascriptSrc.size() > 0;
	}

	@Override
	public boolean inCSharp()
	{
		return csharpSrc.size() > 0;
	}

	public final ArrayList<String> fromModules = new ArrayList<String>();

	public final File featureTexFile;

	public final String featureLabelName;

	public FeatureEntry(String featureName, String featureLabelName, File featureTexFile)
	{
		this.featureName = featureName;
		this.featureLabelName = featureLabelName;
		this.featureTexFile = featureTexFile;
	}

	@Override
	public File getTexFile()
	{
		return featureTexFile;
	}
}
