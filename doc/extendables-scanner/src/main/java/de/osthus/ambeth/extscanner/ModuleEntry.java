package de.osthus.ambeth.extscanner;

import java.io.File;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;

public class ModuleEntry
{
	public final String moduleName;

	public final HashSet<String> javaFiles = new HashSet<String>();

	public final HashSet<String> csharpFiles = new HashSet<String>();

	public final ArrayList<FeatureEntry> features = new ArrayList<FeatureEntry>();

	public boolean inJava()
	{
		return javaFiles.size() > 0;
	}

	public boolean inCSharp()
	{
		return csharpFiles.size() > 0;
	}

	public final File moduleTexFile;

	public ModuleEntry(String moduleName, File moduleTexFile)
	{
		this.moduleName = moduleName;
		this.moduleTexFile = moduleTexFile;
	}
}
