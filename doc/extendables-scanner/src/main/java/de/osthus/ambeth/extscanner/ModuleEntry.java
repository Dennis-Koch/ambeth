package de.osthus.ambeth.extscanner;

import java.io.File;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;

public class ModuleEntry implements IMultiPlatformFeature
{
	public final String moduleName;

	public final HashSet<TypeEntry> javaFiles = new HashSet<TypeEntry>();

	public final HashSet<TypeEntry> javascriptFiles = new HashSet<TypeEntry>();

	public final HashSet<TypeEntry> csharpFiles = new HashSet<TypeEntry>();

	public final HashSet<FeatureEntry> features = new HashSet<FeatureEntry>();

	public final HashSet<String> mavenModules = new HashSet<String>();

	public final ArrayList<ConfigurationEntry> configurations = new ArrayList<ConfigurationEntry>();

	@Override
	public boolean inJava()
	{
		return javaFiles.size() > 0;
	}

	@Override
	public boolean inJavascript()
	{
		return javascriptFiles.size() > 0;
	}

	@Override
	public boolean inCSharp()
	{
		return csharpFiles.size() > 0;
	}

	public final File moduleTexFile;

	public final String labelName;

	public ModuleEntry(String moduleName, String labelName, File moduleTexFile)
	{
		this.moduleName = moduleName;
		this.labelName = labelName;
		this.moduleTexFile = moduleTexFile;
	}
}
