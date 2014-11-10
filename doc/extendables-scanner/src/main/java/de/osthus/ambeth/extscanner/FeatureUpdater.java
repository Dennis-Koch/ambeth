package de.osthus.ambeth.extscanner;

import java.io.File;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.TypeDescription;

public class FeatureUpdater extends AbstractLatexScanner implements IStartingBean
{
	public static final Pattern featureConditionPattern = Pattern.compile(".*%%\\s*feature-condition\\s*=\\s*(\\S+)\\s+.*", Pattern.DOTALL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IModel model;

	@Property(name = "target-feature-tex-dir")
	protected String targetFeatureTexDirPath;

	protected void writeToFeatureFile(FeatureEntry featureEntry, File targetFile) throws Exception
	{
		String targetOpening = getAPI(featureEntry);
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
	protected void buildModel(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable
	{
		try
		{
			File targetFeatureTexDir = new File(targetFeatureTexDirPath).getCanonicalFile();

			log.debug("TargetFeatureTexDir: " + targetFeatureTexDir);

			handleFeatureTexFiles(targetFeatureTexDir);

			for (Entry<String, TypeDescription> entry : javaTypes)
			{
				TypeDescription typeDescr = entry.getValue();
				FeatureEntry featureEntry = findFeatureEntry(typeDescr);
				if (featureEntry == null)
				{
					continue;
				}
				featureEntry.javaSrc = typeDescr;
			}

			for (Entry<String, TypeDescription> entry : csharpTypes)
			{
				TypeDescription typeDescr = entry.getValue();
				FeatureEntry featureEntry = findFeatureEntry(typeDescr);
				if (featureEntry == null)
				{
					continue;
				}
				featureEntry.csharpSrc = typeDescr;
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected void handleModel() throws Throwable
	{
		for (FeatureEntry featureEntry : model.allFeatures())
		{
			log.debug("Handling " + featureEntry.featureTexFile);

			writeToFeatureFile(featureEntry, featureEntry.featureTexFile);
		}
	}

	protected void handleFeatureTexFiles(File currFile)
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
				handleFeatureTexFiles(child);
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
		String labelName = readLabelName(currFile);
		model.addFeature(featureName, new FeatureEntry(featureName, labelName, currFile));
	}

	protected FeatureEntry findFeatureEntry(TypeDescription typeDescr)
	{
		String name = typeDescr.getName();
		FeatureEntry featureEntry = model.resolveFeature("I" + name);
		if (featureEntry == null)
		{
			featureEntry = model.resolveFeature(name);
		}
		if (featureEntry == null && name.startsWith("I"))
		{
			featureEntry = model.resolveFeature(name.substring(1));
		}
		return featureEntry;
	}
}
