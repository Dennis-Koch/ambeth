package com.koch.ambeth.extscanner.model;

import java.io.File;

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;

public class ModuleEntry implements IMultiPlatformFeature, Comparable<ModuleEntry> {
	public final String moduleName;

	public final HashSet<TypeEntry> javaFiles = new HashSet<>();

	public final HashSet<TypeEntry> javascriptFiles = new HashSet<>();

	public final HashSet<TypeEntry> csharpFiles = new HashSet<>();

	public final HashSet<FeatureEntry> features = new HashSet<>();

	public final HashSet<String> mavenModules = new HashSet<>();

	public final ArrayList<ConfigurationEntry> configurations = new ArrayList<>();

	@Override
	public boolean inJava() {
		return !javaFiles.isEmpty();
	}

	@Override
	public boolean inJavascript() {
		return !javascriptFiles.isEmpty();
	}

	@Override
	public boolean inCSharp() {
		return !csharpFiles.isEmpty();
	}

	public final File moduleTexFile;

	public final String labelName;

	public ModuleEntry(String moduleName, String labelName, File moduleTexFile) {
		this.moduleName = moduleName;
		this.labelName = labelName;
		this.moduleTexFile = moduleTexFile;
	}

	@Override
	public int compareTo(ModuleEntry o) {
		return moduleName.compareTo(o.moduleName);
	}
}
