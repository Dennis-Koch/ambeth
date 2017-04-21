package com.koch.ambeth.extscanner;

import java.io.File;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.extscanner.model.FeatureEntry;
import com.koch.ambeth.extscanner.model.TypeEntry;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.classbrowser.java.TypeDescription;

public class FeatureUpdater extends AbstractLatexScanner implements IStartingBean {
	public static final Pattern featureConditionPattern =
			Pattern.compile(".*%%\\s*feature-condition\\s*=\\s*(\\S+)\\s+.*", Pattern.DOTALL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IModel model;

	@Property(name = "target-feature-tex-dir")
	protected String targetFeatureTexDirPath;

	protected void writeToFeatureFile(FeatureEntry featureEntry, File targetFile) throws Exception {
		String targetOpening = getAPI(featureEntry);
		StringBuilder sb = readFileFully(targetFile);
		String newContent = writeSetAPI(sb, featureEntry);
		if (newContent.contentEquals(sb)) {
			return;
		}
		if (targetOpening.length() == 0) {
			log.warn("Feature-component not found any more: " + targetFile);
			return;
		}
		updateFileFully(targetFile, newContent);
	}

	@Override
	protected void buildModel(IMap<String, TypeDescription> javaTypes,
			IMap<String, TypeDescription> csharpTypes) throws Throwable {
		try {
			File targetFeatureTexDir = new File(targetFeatureTexDirPath).getCanonicalFile();

			log.debug("TargetFeatureTexDir: " + targetFeatureTexDir);

			handleFeatureTexFiles(targetFeatureTexDir);

			for (Entry<String, TypeDescription> entry : javaTypes) {
				TypeDescription typeDescr = entry.getValue();
				FeatureEntry[] featureEntries = findFeatureEntry(typeDescr);
				for (FeatureEntry featureEntry : featureEntries) {
					TypeEntry typeEntry = model.resolveTypeEntry(typeDescr);
					featureEntry.javaSrc.add(typeEntry);
				}
			}

			for (Entry<String, TypeDescription> entry : csharpTypes) {
				TypeDescription typeDescr = entry.getValue();
				FeatureEntry[] featureEntries = findFeatureEntry(typeDescr);
				for (FeatureEntry featureEntry : featureEntries) {
					TypeEntry typeEntry = model.resolveTypeEntry(typeDescr);
					featureEntry.csharpSrc.add(typeEntry);
				}
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected void handleModel() throws Throwable {
		for (FeatureEntry featureEntry : model.allFeatures()) {
			log.debug("Handling " + featureEntry.featureTexFile);

			writeToFeatureFile(featureEntry, featureEntry.featureTexFile);
		}
	}

	protected void handleFeatureTexFiles(File currFile) {
		if (currFile.isDirectory()) {
			File[] children = currFile.listFiles();
			if (children == null) {
				return;
			}
			for (File child : children) {
				handleFeatureTexFiles(child);
			}
			return;
		}
		Matcher matcher = texFilePattern.matcher(currFile.getName());
		if (!matcher.matches()) {
			return;
		}
		String featureName = matcher.group(1);
		StringBuilder content = readFileFully(currFile);
		Matcher featureConditionMatcher = featureConditionPattern.matcher(content);
		if (featureConditionMatcher.matches()) {
			featureName = featureConditionMatcher.group(1);
		}
		String[] featureNames = featureName.split(";");
		String labelName = readLabelName(currFile);
		FeatureEntry featureEntry = new FeatureEntry(featureName, labelName, currFile);
		for (String featureNameItem : featureNames) {
			featureEntry.typeConditions.add(featureNameItem);
			featureEntry.typeConditions.add("I" + featureNameItem);
		}
		model.addFeature(labelName, featureEntry);
	}

	protected FeatureEntry[] findFeatureEntry(TypeDescription typeDescr) {
		TypeEntry typeEntry = model.resolveTypeEntry(typeDescr);
		return model.resolveFeaturesByType(typeEntry);
	}
}
