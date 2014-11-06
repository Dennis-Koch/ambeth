package de.osthus.ambeth.extscanner;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.TypeDescription;

public class FeatureUpdater extends AbstractLatexScanner implements IStartingBean, IFeatureUpdater
{
	public static final Pattern labelNamePattern = Pattern.compile(".*\\\\section\\{[^\\}]*\\}\\s*\\\\label\\{([^\\}]*)\\}.*", Pattern.DOTALL);

	public static final Pattern featureConditionPattern = Pattern.compile(".*%%\\s*feature-condition\\s*=\\s*(\\S+)\\s+.*", Pattern.DOTALL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = "target-feature-tex-dir")
	protected String targetFeatureTexDirPath;

	protected void writeToFeatureFile(FeatureEntry featureEntry, File targetFile) throws Exception
	{
		String targetOpening;
		if (featureEntry.inJava())
		{
			if (featureEntry.inCSharp())
			{
				targetOpening = availableInJavaAndCsharpOpening;
			}
			else
			{
				targetOpening = availableInJavaOnlyOpening;
			}
		}
		else if (featureEntry.inCSharp())
		{
			targetOpening = availableInCsharpOnlyOpening;
		}
		else
		{
			targetOpening = "";
		}
		StringBuilder sb = readFileFully(targetFile);
		String newContent = replaceAllAvailables.matcher(sb).replaceAll(Matcher.quoteReplacement(targetOpening));
		if (newContent.contentEquals(sb))
		{
			return;
		}
		if (targetOpening.length() == 0)
		{
			log.warn("Feature-component not found any more: " + targetFile);
			return;
		}
		updateFileFully(targetFile, newContent);
	}

	@Override
	protected void handle(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable
	{
		IMap<String, FeatureEntry> nameToFeatureMap = buildFeatureMap(javaTypes, csharpTypes);
		for (Entry<String, FeatureEntry> entry : nameToFeatureMap)
		{
			FeatureEntry featureEntry = entry.getValue();
			log.debug("Handling " + featureEntry.featureTexFile);

			writeToFeatureFile(featureEntry, featureEntry.featureTexFile);
		}
	}

	@Override
	public IMap<String, FeatureEntry> buildFeatureMap(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes)
	{
		try
		{
			File targetFeatureTexDir = new File(targetFeatureTexDirPath).getCanonicalFile();

			log.debug("TargetFeatureTexDir: " + targetFeatureTexDir);

			HashMap<String, FeatureEntry> nameToFeatureMap = new HashMap<String, FeatureEntry>();
			handleFeatureTexFiles(targetFeatureTexDir, nameToFeatureMap);

			for (Entry<String, TypeDescription> entry : javaTypes)
			{
				TypeDescription typeDescr = entry.getValue();
				FeatureEntry featureEntry = findFeatureEntry(typeDescr, nameToFeatureMap);
				if (featureEntry == null)
				{
					continue;
				}
				featureEntry.javaSrc = typeDescr;
			}

			for (Entry<String, TypeDescription> entry : csharpTypes)
			{
				TypeDescription typeDescr = entry.getValue();
				FeatureEntry featureEntry = findFeatureEntry(typeDescr, nameToFeatureMap);
				if (featureEntry == null)
				{
					continue;
				}
				featureEntry.csharpSrc = typeDescr;
			}
			return nameToFeatureMap;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void handleFeatureTexFiles(File currFile, Map<String, FeatureEntry> nameToFeatureMap)
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
				handleFeatureTexFiles(child, nameToFeatureMap);
			}
			return;
		}
		Matcher matcher = texFilePattern.matcher(currFile.getName());
		if (!matcher.matches())
		{
			return;
		}
		String featureName = matcher.group(1);
		StringBuilder content = readFileFully(currFile);
		Matcher featureConditionMatcher = featureConditionPattern.matcher(content);
		if (featureConditionMatcher.matches())
		{
			featureName = featureConditionMatcher.group(1);
		}
		if (nameToFeatureMap.containsKey(featureName))
		{
			throw new IllegalStateException("Feature name not unique: " + currFile + " vs. " + nameToFeatureMap.get(featureName).featureTexFile);
		}
		String labelName = readLabelName(currFile);
		nameToFeatureMap.put(featureName, new FeatureEntry(featureName, labelName, currFile));
	}

	protected String readLabelName(File currFile)
	{
		StringBuilder sb = readFileFully(currFile);
		Matcher matcher = labelNamePattern.matcher(sb);
		if (!matcher.matches())
		{
			log.warn("Label not found in file " + currFile);
			return null;
		}
		return matcher.group(1);
	}

	protected FeatureEntry findFeatureEntry(TypeDescription typeDescr, Map<String, FeatureEntry> nameToFeatureMap)
	{
		String name = typeDescr.getName();
		FeatureEntry featureEntry = nameToFeatureMap.get("I" + name);
		if (featureEntry == null)
		{
			featureEntry = nameToFeatureMap.get(name);
		}
		if (featureEntry == null && name.startsWith("I"))
		{
			featureEntry = nameToFeatureMap.get(name.substring(1));
		}
		return featureEntry;
	}
}
