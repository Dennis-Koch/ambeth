package de.osthus.ambeth.extscanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.TypeDescription;

public class ModuleUpdater extends AbstractLatexScanner implements IStartingBean
{
	public static final String FEATURES_START = "%% FEATURES START";

	public static final String FEATURES_GENERATED_START = "%% FEATURES GENERATED START";

	public static final String FEATURES_END = "%% FEATURES END";

	public static final Pattern replaceGeneratedFeaturesPattern = Pattern.compile(
			"(.*" + Pattern.quote(FEATURES_GENERATED_START) + ").*(" + Pattern.quote(FEATURES_END) + ".*)", Pattern.DOTALL);

	public static final Pattern replaceManualFeaturesPattern = Pattern.compile(
			".*" + Pattern.quote(FEATURES_START) + "(.*)" + Pattern.quote(FEATURES_GENERATED_START) + ".*", Pattern.DOTALL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IFeatureUpdater featureUpdater;

	@Property(name = "target-module-tex-dir")
	protected String targetModuleTexDirPath;

	protected void writeToModuleFile(ModuleEntry moduleEntry, File targetFile) throws Exception
	{
		String targetOpening;
		if (moduleEntry.inJava())
		{
			if (moduleEntry.inCSharp())
			{
				targetOpening = availableInJavaAndCsharpOpening;
			}
			else
			{
				targetOpening = availableInJavaOnlyOpening;
			}
		}
		else if (moduleEntry.inCSharp())
		{
			targetOpening = availableInCsharpOnlyOpening;
		}
		else
		{
			targetOpening = "";
		}
		StringBuilder sb = readFileFully(targetFile);
		String newContent = replaceAllAvailables.matcher(sb).replaceAll(Matcher.quoteReplacement(targetOpening));
		Matcher matcher = replaceGeneratedFeaturesPattern.matcher(newContent);
		Matcher manualMatcher = replaceManualFeaturesPattern.matcher(newContent);
		String manualContent = null;
		if (manualMatcher.matches())
		{
			manualContent = manualMatcher.group(1);
		}
		if (matcher.matches() && manualContent != null)
		{
			newContent = matcher.group(1) + "\n" + generatedFeatures(manualContent, moduleEntry) + "\t" + matcher.group(2);
		}
		else
		{
			log.warn("Could not replace generated features in '" + targetFile.getPath() + "'");
		}
		if (newContent.contentEquals(sb))
		{
			return;
		}
		if (targetOpening.length() == 0)
		{
			log.warn("Module not found any more: " + targetFile);
			return;
		}
		updateFileFully(targetFile, newContent);
	}

	protected String generatedFeatures(String manualContent, ModuleEntry moduleEntry)
	{
		BufferedReader rd = new BufferedReader(new StringReader(manualContent));
		HashSet<String> existingLabels = new HashSet<String>();
		String line;
		try
		{
			Pattern featureLabelPattern = Pattern.compile("\\s*\\\\item\\s*\\\\prettyref\\{([^\\}]+)\\}\\s*");
			while ((line = rd.readLine()) != null)
			{
				Matcher lineMatcher = featureLabelPattern.matcher(line);
				if (!lineMatcher.matches())
				{
					continue;
				}
				existingLabels.add(lineMatcher.group(1));
			}
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		ArrayList<FeatureEntry> features = new ArrayList<FeatureEntry>(moduleEntry.features);
		Collections.sort(features, new Comparator<FeatureEntry>()
		{
			@Override
			public int compare(FeatureEntry o1, FeatureEntry o2)
			{
				return o1.featureName.compareTo(o2.featureName);
			}
		});
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = features.size(); a < size; a++)
		{
			FeatureEntry featureEntry = features.get(a);
			if (featureEntry.featureLabelName == null)
			{
				continue;
			}
			if (existingLabels.contains(featureEntry.featureLabelName))
			{
				continue;
			}
			sb.append("\t%%\\item \\prettyref{").append(featureEntry.featureLabelName).append("}\n");
		}
		return sb.toString();
	}

	@Override
	protected void handle(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable
	{
		File targetModuleTexDir = new File(targetModuleTexDirPath).getCanonicalFile();

		log.debug("TargetModuleTexDir: " + targetModuleTexDir);

		HashMap<String, ModuleEntry> nameToModuleMap = new HashMap<String, ModuleEntry>();

		handleModuleTexFiles(targetModuleTexDir, nameToModuleMap);

		for (Entry<String, TypeDescription> entry : javaTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			ModuleEntry moduleEntry = findModuleEntry(typeDescr, nameToModuleMap);
			if (moduleEntry == null)
			{
				continue;
			}
			moduleEntry.javaFiles.add(typeDescr.getModuleName());
		}

		for (Entry<String, TypeDescription> entry : csharpTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			ModuleEntry moduleEntry = findModuleEntry(typeDescr, nameToModuleMap);
			if (moduleEntry == null)
			{
				continue;
			}
			moduleEntry.csharpFiles.add(typeDescr.getModuleName());
		}
		IMap<String, FeatureEntry> nameToFeatureMap = featureUpdater.buildFeatureMap(javaTypes, csharpTypes);
		for (Entry<String, FeatureEntry> entry : nameToFeatureMap)
		{
			FeatureEntry featureEntry = entry.getValue();
			ModuleEntry moduleEntry = null;
			if (featureEntry.csharpSrc != null)
			{
				moduleEntry = findModuleEntry(featureEntry.csharpSrc, nameToModuleMap);
			}
			if (moduleEntry == null && featureEntry.javaSrc != null)
			{
				moduleEntry = findModuleEntry(featureEntry.javaSrc, nameToModuleMap);
			}
			if (moduleEntry == null)
			{
				continue;
			}
			moduleEntry.features.add(featureEntry);
		}
		for (Entry<String, ModuleEntry> entry : nameToModuleMap)
		{
			ModuleEntry moduleEntry = entry.getValue();
			log.debug("Handling " + moduleEntry.moduleTexFile);

			writeToModuleFile(moduleEntry, moduleEntry.moduleTexFile);
		}
	}

	protected void handleModuleTexFiles(File currFile, Map<String, ModuleEntry> nameToModuleMap)
	{
		if (currFile.isDirectory())
		{
			File[] children = currFile.listFiles();
			if (children == null)
			{
				return;
			}
			for (File child : children)
			{
				handleModuleTexFiles(child, nameToModuleMap);
			}
			return;
		}
		Pattern texFilePattern = Pattern.compile("(.+)\\.tex");
		Matcher matcher = texFilePattern.matcher(currFile.getName());
		if (!matcher.matches())
		{
			return;
		}
		String moduleName = matcher.group(1).toLowerCase();
		if (nameToModuleMap.containsKey(moduleName))
		{
			throw new IllegalStateException("Module name not unique: " + currFile + " vs. " + nameToModuleMap.get(moduleName).moduleTexFile);
		}
		nameToModuleMap.put(moduleName, new ModuleEntry(matcher.group(1), currFile));
	}

	protected ModuleEntry findModuleEntry(TypeDescription typeDescr, Map<String, ModuleEntry> nameToModuleMap)
	{
		String moduleName = typeDescr.getModuleName().toLowerCase();
		ModuleEntry moduleEntry = nameToModuleMap.get(moduleName);
		if (moduleEntry == null && moduleName.startsWith("jambeth"))
		{
			moduleName = moduleName.substring("jambeth".length());
			moduleEntry = nameToModuleMap.get(moduleName);
		}
		if (moduleEntry == null && moduleName.startsWith("ambeth"))
		{
			moduleName = moduleName.substring("ambeth".length());
			moduleEntry = nameToModuleMap.get(moduleName);
		}
		if (moduleEntry == null && (moduleName.startsWith(".") || moduleName.startsWith("-")))
		{
			moduleName = moduleName.substring(1);
			moduleEntry = nameToModuleMap.get(moduleName);
		}
		if (moduleEntry == null && moduleName.contains("-"))
		{
			int indexOf = moduleName.indexOf('-');
			moduleName = moduleName.substring(0, indexOf);
			moduleEntry = nameToModuleMap.get(moduleName);
		}
		return moduleEntry;
	}
}
