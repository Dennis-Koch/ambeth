package com.koch.ambeth.extscanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.extscanner.model.ConfigurationEntry;
import com.koch.ambeth.extscanner.model.FeatureEntry;
import com.koch.ambeth.extscanner.model.ModuleEntry;
import com.koch.ambeth.extscanner.model.TypeEntry;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.classbrowser.java.TypeDescription;

public class ModuleUpdater extends AbstractLatexScanner implements IStartingBean {
	public static final Pattern featureLabelPattern =
			Pattern.compile("\\s*\\\\item\\s*\\\\prettyref\\{([^\\}]+)\\}\\s*");

	public static final Pattern texFilePattern = Pattern.compile("(.+)\\.tex");

	public static final String FEATURES_START = "%% FEATURES START";

	public static final String FEATURES_GENERATED_START = "%% FEATURES GENERATED START";

	public static final String FEATURES_END = "%% FEATURES END";

	public static final String CONFIGURATION_START = "%% CONFIGURATION START";

	public static final String CONFIGURATION_GENERATED_START = "%% CONFIGURATION GENERATED START";

	public static final String CONFIGURATION_END = "%% CONFIGURATION END";

	public static final String MAVEN_GENERATED_START = "%% MAVEN GENERATED START";

	public static final String MAVEN_END = "%% MAVEN END";

	public static final Pattern replaceGeneratedFeaturesPattern =
			Pattern.compile("(.*" + Pattern.quote(FEATURES_GENERATED_START) + ").*("
					+ Pattern.quote(FEATURES_END) + ".*)", Pattern.DOTALL);

	public static final Pattern replaceManualFeaturesPattern =
			Pattern.compile(".*" + Pattern.quote(FEATURES_START) + "(.*)"
					+ Pattern.quote(FEATURES_GENERATED_START) + ".*", Pattern.DOTALL);

	public static final Pattern replaceGeneratedConfigurationPattern =
			Pattern.compile("(.*" + Pattern.quote(CONFIGURATION_GENERATED_START) + ").*("
					+ Pattern.quote(CONFIGURATION_END) + ".*)", Pattern.DOTALL);

	public static final Pattern replaceManualConfigurationPattern =
			Pattern.compile(".*" + Pattern.quote(CONFIGURATION_START) + "(.*)"
					+ Pattern.quote(CONFIGURATION_GENERATED_START) + ".*", Pattern.DOTALL);

	public static final Pattern replaceGeneratedMavenPattern = Pattern.compile(
			"(.*" + Pattern.quote(MAVEN_GENERATED_START) + ").*(" + Pattern.quote(MAVEN_END) + ".*)",
			Pattern.DOTALL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IModel model;

	@Property(name = "target-module-tex-dir")
	protected String targetModuleTexDirPath;

	protected void writeToModuleFile(ModuleEntry moduleEntry, File targetFile) throws Exception {
		String targetOpening = getAPI(moduleEntry);
		StringBuilder sb = readFileFully(targetFile);
		String newContent = writeSetAPI(sb, moduleEntry);
		Matcher matcher = replaceGeneratedMavenPattern.matcher(newContent);
		if (matcher.matches()) {
			newContent = matcher.group(1) + "\n" + generatedMaven(moduleEntry) + matcher.group(2);
		}
		else {
			log.warn("Could not replace generated features in '" + targetFile.getPath() + "'");
		}
		matcher = replaceGeneratedFeaturesPattern.matcher(newContent);
		Matcher manualMatcher = replaceManualFeaturesPattern.matcher(newContent);
		String manualContent = null;
		if (manualMatcher.matches()) {
			manualContent = manualMatcher.group(1);
		}
		if (matcher.matches()) {
			newContent = matcher.group(1) + "\n" + generatedFeatures(manualContent, moduleEntry) + "\t"
					+ matcher.group(2);
		}
		else {
			log.warn("Could not replace generated features in '" + targetFile.getPath() + "'");
		}
		matcher = replaceGeneratedConfigurationPattern.matcher(newContent);
		manualMatcher = replaceManualConfigurationPattern.matcher(newContent);
		manualContent = null;
		if (manualMatcher.matches()) {
			manualContent = manualMatcher.group(1);
		}
		if (matcher.matches()) {
			newContent = matcher.group(1) + "\n" + generatedConfigurations(manualContent, moduleEntry)
					+ matcher.group(2);
		}
		else {
			log.warn("Could not replace generated configuration in '" + targetFile.getPath() + "'");
		}
		if (newContent.contentEquals(sb)) {
			return;
		}
		if (targetOpening.length() == 0) {
			log.warn("Module not found any more: " + targetFile);
			return;
		}
		updateFileFully(targetFile, newContent);
	}

	protected StringBuilder generatedMaven(ModuleEntry moduleEntry) {
		ArrayList<String> mavenModules = new ArrayList<>(moduleEntry.mavenModules);
		Collections.sort(mavenModules);

		StringBuilder sb = new StringBuilder();
		if (mavenModules.isEmpty()) {
			return sb;
		}
		sb.append("\\begin{lstlisting}[style=POM,caption={Maven modules to use \\emph{Ambeth ")
				.append(moduleEntry.moduleName).append("}}]\n");
		for (int a = 0, size = mavenModules.size(); a < size; a++) {
			if (a > 0) {
				sb.append("\n");
			}
			sb.append("<dependency>\n");
			sb.append("\t<groupId>com.koch.ambeth</groupId>\n");
			sb.append("\t<artifactId>").append(mavenModules.get(a)).append("</artifactId>\n");
			sb.append("\t<version>§\\version§</version>\n");
			sb.append("</dependency>\n");
		}
		sb.append("\\end{lstlisting}\n");
		return sb;
	}

	protected StringBuilder generatedConfigurations(String manualContent, ModuleEntry moduleEntry) {
		ISet<String> existingLabels = readExistingLabels(manualContent);
		ArrayList<ConfigurationEntry> configurations = new ArrayList<>(moduleEntry.configurations);
		Collections.sort(configurations, new Comparator<ConfigurationEntry>() {
			@Override
			public int compare(ConfigurationEntry o1, ConfigurationEntry o2) {
				return o1.propertyName.compareTo(o2.propertyName);
			}
		});
		StringBuilder sb = new StringBuilder();

		if (configurations.isEmpty()) {
			return sb;
		}
		sb.append("\\subsection{Configuration}\n");
		sb.append("\\begin{itemize}\n");

		for (ConfigurationEntry configurationEntry : configurations) {
			if (configurationEntry.labelName == null) {
				continue;
			}
			if (existingLabels.contains(configurationEntry.labelName)) {
				continue;
			}
			if (manualContent == null || manualContent.length() == 0) {
				sb.append("\t");
			}
			else {
				sb.append("\t%%");
			}
			sb.append("\\item \\prettyref{").append(configurationEntry.labelName).append("}\n");
		}
		sb.append("\\end{itemize}\n");
		return sb;
	}

	protected ISet<String> readExistingLabels(String manualContent) {
		HashSet<String> existingLabels = new HashSet<>();
		if (manualContent == null || manualContent.length() == 0) {
			return existingLabels;
		}
		BufferedReader rd = new BufferedReader(new StringReader(manualContent));
		String line;
		try {
			while ((line = rd.readLine()) != null) {
				Matcher lineMatcher = featureLabelPattern.matcher(line);
				if (!lineMatcher.matches()) {
					continue;
				}
				existingLabels.add(lineMatcher.group(1));
			}
		}
		catch (IOException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		return existingLabels;
	}

	protected String generatedFeatures(String manualContent, ModuleEntry moduleEntry) {
		ISet<String> existingLabels = readExistingLabels(manualContent);
		ArrayList<FeatureEntry> features = new ArrayList<>(moduleEntry.features);
		Collections.sort(features, new Comparator<FeatureEntry>() {
			@Override
			public int compare(FeatureEntry o1, FeatureEntry o2) {
				return o1.featureName.compareTo(o2.featureName);
			}
		});
		StringBuilder sb = new StringBuilder();
		for (FeatureEntry featureEntry : features) {
			if (featureEntry.featureLabelName == null) {
				continue;
			}
			if (existingLabels.contains(featureEntry.featureLabelName)) {
				continue;
			}
			if (manualContent == null || manualContent.length() == 0) {
				sb.append("\t");
			}
			else {
				sb.append("\t%%");
			}
			sb.append("\\item \\prettyref{").append(featureEntry.featureLabelName).append("}\n");
		}
		return sb.toString();
	}

	@Override
	protected void buildModel(IMap<String, TypeDescription> javaTypes,
			IMap<String, TypeDescription> csharpTypes) throws Throwable {
		File targetModuleTexDir = new File(targetModuleTexDirPath).getCanonicalFile();

		log.debug("TargetModuleTexDir: " + targetModuleTexDir);

		handleModuleTexFiles(targetModuleTexDir);

		for (Entry<String, TypeDescription> entry : javaTypes) {
			TypeDescription typeDescr = entry.getValue();
			ModuleEntry moduleEntry = model.resolveModule(typeDescr.getModuleName());
			if (moduleEntry == null) {
				continue;
			}
			TypeEntry typeEntry = model.resolveTypeEntry(typeDescr);
			typeEntry.moduleEntry = moduleEntry;
			moduleEntry.javaFiles.add(typeEntry);
		}

		for (Entry<String, TypeDescription> entry : csharpTypes) {
			TypeDescription typeDescr = entry.getValue();
			ModuleEntry moduleEntry = model.resolveModule(typeDescr.getModuleName());
			if (moduleEntry == null) {
				continue;
			}
			TypeEntry typeEntry = model.resolveTypeEntry(typeDescr);
			typeEntry.moduleEntry = moduleEntry;
			moduleEntry.csharpFiles.add(typeEntry);
		}
	}

	@Override
	protected void handleModel() throws Throwable {
		for (FeatureEntry featureEntry : model.allFeatures()) {
			for (TypeEntry typeEntry : featureEntry.csharpSrc) {
				ModuleEntry moduleEntry = typeEntry.moduleEntry;
				if (moduleEntry == null) {
					continue;
				}
				moduleEntry.features.add(featureEntry);
			}
			for (TypeEntry typeEntry : featureEntry.javaSrc) {
				ModuleEntry moduleEntry = typeEntry.moduleEntry;
				if (moduleEntry == null) {
					continue;
				}
				moduleEntry.features.add(featureEntry);
			}
			for (TypeEntry typeEntry : featureEntry.javascriptSrc) {
				ModuleEntry moduleEntry = typeEntry.moduleEntry;
				if (moduleEntry == null) {
					continue;
				}
				moduleEntry.features.add(featureEntry);
			}
		}
		for (ConfigurationEntry configurationEntry : model.allConfigurations()) {
			ModuleEntry moduleEntry = model.resolveModule(configurationEntry.moduleName);
			if (moduleEntry == null) {
				continue;
			}
			moduleEntry.configurations.add(configurationEntry);
		}
		for (ModuleEntry moduleEntry : model.allModules()) {
			log.debug("Handling " + moduleEntry.moduleTexFile);

			writeToModuleFile(moduleEntry, moduleEntry.moduleTexFile);
		}
	}

	protected void handleModuleTexFiles(File currFile) {
		if (currFile.isDirectory()) {
			File[] children = currFile.listFiles();
			if (children == null) {
				return;
			}
			for (File child : children) {
				handleModuleTexFiles(child);
			}
			return;
		}
		Matcher matcher = texFilePattern.matcher(currFile.getName());
		if (!matcher.matches()) {
			return;
		}
		String moduleName = matcher.group(1).toLowerCase();
		String labelName = readLabelName(currFile);
		model.addModule(moduleName, new ModuleEntry(matcher.group(1), labelName, currFile));
	}
}
