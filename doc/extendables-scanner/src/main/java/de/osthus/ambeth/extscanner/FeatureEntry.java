package de.osthus.ambeth.extscanner;

import java.io.File;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.classbrowser.java.TypeDescription;

public class FeatureEntry
{
	public TypeDescription javaSrc;

	public TypeDescription csharpSrc;

	public String javaFile;

	public String csharpFile;

	public final String featureName;

	public boolean inJava()
	{
		return javaSrc != null;
	}

	public boolean inCSharp()
	{
		return csharpSrc != null;
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
}
