package com.koch.ambeth.extscanner.model;

import java.io.File;

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;

public class FeatureEntry implements IMultiPlatformFeature, ITexFileAware {
	public final HashSet<TypeEntry> javaSrc = new HashSet<>();

	public final HashSet<TypeEntry> javascriptSrc = new HashSet<>();

	public final HashSet<TypeEntry> csharpSrc = new HashSet<>();

	public final HashSet<String> typeConditions = new HashSet<>();

	public String javaFile;

	public String csharpFile;

	public final String featureName;

	@Override
	public boolean inJava() {
		return !javaSrc.isEmpty();
	}

	@Override
	public boolean inJavascript() {
		return !javascriptSrc.isEmpty();
	}

	@Override
	public boolean inCSharp() {
		return !csharpSrc.isEmpty();
	}

	public final ArrayList<String> fromModules = new ArrayList<>();

	public final File featureTexFile;

	public final String featureLabelName;

	public FeatureEntry(String featureName, String featureLabelName, File featureTexFile) {
		this.featureName = featureName;
		this.featureLabelName = featureLabelName;
		this.featureTexFile = featureTexFile;
	}

	@Override
	public File getTexFile() {
		return featureTexFile;
	}
}
