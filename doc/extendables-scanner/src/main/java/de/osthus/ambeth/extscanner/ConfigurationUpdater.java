package de.osthus.ambeth.extscanner;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.AnnotationParamInfo;
import de.osthus.classbrowser.java.FieldDescription;
import de.osthus.classbrowser.java.MethodDescription;
import de.osthus.classbrowser.java.TypeDescription;

public class ConfigurationUpdater extends AbstractLatexScanner implements IStartingBean
{
	// public static final Pattern csharpConstantPattern = Pattern.compile(" *public *const *String *[^ =]+ *= *\"(.+)\" *; *");
	//
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = "properties-tex-file")
	protected String allPropertiesTexFilePath;

	@Property(name = "target-properties-tex-dir")
	protected String targetPropertiesTexDirPath;

	@Override
	protected void handle(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable
	{
		File allPropertiesTexFile = new File(allPropertiesTexFilePath).getCanonicalFile();
		allPropertiesTexFile.getParentFile().mkdirs();

		File targetPropertiesTexDir = new File(targetPropertiesTexDirPath).getCanonicalFile();
		targetPropertiesTexDir.mkdirs();

		log.debug("TargetTexFile: " + allPropertiesTexFile);
		log.debug("PropertiesTexDir: " + targetPropertiesTexDir);

		String targetExtendableTexDirCP = targetPropertiesTexDir.getPath();
		String targetTexFileCP = allPropertiesTexFile.getParent();
		if (!targetExtendableTexDirCP.startsWith(targetTexFileCP))
		{
			throw new IllegalStateException("Path '" + targetExtendableTexDirCP + "' must reside within '" + targetTexFileCP + "'");
		}
		String pathToExtendableTexFile = targetExtendableTexDirCP.substring(targetTexFileCP.length() + 1);

		HashMap<String, ConfigurationEntry> nameToConfigurationMap = new HashMap<String, ConfigurationEntry>();

		for (Entry<String, TypeDescription> entry : javaTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			if (!typeDescr.getName().endsWith("ConfigurationConstants"))
			{
				continue;
			}
			handleConfigurationConstants(typeDescr, true, nameToConfigurationMap);
		}

		for (Entry<String, TypeDescription> entry : csharpTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			if (!typeDescr.getName().endsWith("ConfigurationConstants"))
			{
				continue;
			}
			handleConfigurationConstants(typeDescr, false, nameToConfigurationMap);
		}

		scanForConfigurationUsage(javaTypes, true, nameToConfigurationMap);
		scanForConfigurationUsage(csharpTypes, false, nameToConfigurationMap);

		String[] propertyNames = nameToConfigurationMap.keySet().toArray(String.class);
		Arrays.sort(propertyNames);

		FileWriter fw = new FileWriter(allPropertiesTexFile);
		try
		{
			fw.append("%---------------------------------------------------------------\n");
			fw.append("% This file is FULLY generated. Please do not edit anything here\n");
			fw.append("% Any changes have to be done to the java class " + ConfigurationUpdater.class.getName() + "\n");
			fw.append("%---------------------------------------------------------------\n");
			fw.append("\\chapter{Ambeth Configuration}\n");
			fw.append("\\begin{landscape}\n");
			fw.append("\\begin{longtable}{ l l c c c } \\hline \\textbf{Property} & \\textbf{Default Value} & \\textbf{Mandatory} & \\textbf{Java} & \\textbf{C\\#} \\\\\n");
			fw.append("\t\\endhead\n");
			fw.append("\t\\hline\n");

			ArrayList<String> includes = new ArrayList<String>();

			for (String propertyName : propertyNames)
			{
				ConfigurationEntry configurationEntry = nameToConfigurationMap.get(propertyName);
				log.debug("Handling " + configurationEntry.propertyName);

				if (configurationEntry.isMandatory == null)
				{
					log.warn("Seems like '" + configurationEntry.propertyName + "' is unused?");
					configurationEntry.isMandatory = Boolean.FALSE;
				}

				String texName = buildTexPropertyName(configurationEntry);

				String labelName = "configuration:" + texName;
				writeTableRow(configurationEntry, labelName, fw);
				fw.append(" \\\\\n");
				fw.append("\t\\hline\n");

				String expectedConfigurationTexFileName = texName + ".tex";

				includes.add(pathToExtendableTexFile + "/" + texName);

				File expectedConfigurationTexFile = new File(targetPropertiesTexDir, expectedConfigurationTexFileName);

				writeToConfigurationTexFile(configurationEntry, labelName, expectedConfigurationTexFile);
			}
			fw.append("\\end{longtable}\n");
			fw.append("\\end{landscape}\n");
			for (int a = 0, size = includes.size(); a < size; a++)
			{
				// now write all chapter includes which are referenced from the table
				fw.append("\\input{").append(includes.get(a)).append("}\n");
			}
			fw.append("%--END OF GENERATION----------------------------------------------------------\n");
		}
		finally
		{
			fw.close();
		}
		allPropertiesTexFile.setLastModified(currentTime);
	}

	protected void scanForConfigurationUsage(IMap<String, TypeDescription> types, boolean isJava, IMap<String, ConfigurationEntry> nameToConfigurationMap)
	{
		for (Entry<String, TypeDescription> javaEntry : types)
		{
			TypeDescription typeDescr = javaEntry.getValue();
			for (FieldDescription field : typeDescr.getFieldDescriptions())
			{
				for (de.osthus.classbrowser.java.AnnotationInfo annotation : field.getAnnotations())
				{
					processAnnotation(typeDescr, annotation, isJava, nameToConfigurationMap);
				}
			}
			for (MethodDescription method : typeDescr.getMethodDescriptions())
			{
				for (de.osthus.classbrowser.java.AnnotationInfo annotation : method.getAnnotations())
				{
					processAnnotation(typeDescr, annotation, isJava, nameToConfigurationMap);
				}
			}
		}
	}

	protected void processAnnotation(TypeDescription typeDescr, de.osthus.classbrowser.java.AnnotationInfo annotation, boolean isJava,
			IMap<String, ConfigurationEntry> nameToConfigurationMap)
	{
		if (!Property.class.getName().equals(annotation.getAnnotationType()))
		{
			return;
		}
		String currentValue = null;
		Boolean mandatory = null;
		String defaultValue = null;
		for (AnnotationParamInfo param : annotation.getParameters())
		{
			String name = param.getName();
			if ("name".equals(name))
			{
				currentValue = (String) param.getCurrentValue();
				if (Property.DEFAULT_VALUE.equals(currentValue))
				{
					currentValue = null;
				}
			}
			else if ("mandatory".equals(name))
			{
				mandatory = param.getCurrentValue() != null ? Boolean.valueOf((String) param.getCurrentValue()) : null;
				if (mandatory == null)
				{
					mandatory = param.getDefaultValue() != null ? Boolean.valueOf((String) param.getDefaultValue()) : null;
				}
			}
			else if ("defaultValue".equals(name))
			{
				defaultValue = (String) param.getCurrentValue();
			}
		}
		if (currentValue == null)
		{
			return;
		}
		if (mandatory == null)
		{
			mandatory = Boolean.TRUE;
		}
		ConfigurationEntry configurationEntry = nameToConfigurationMap.get(currentValue);
		if (configurationEntry == null)
		{
			configurationEntry = new ConfigurationEntry(currentValue);
			nameToConfigurationMap.put(currentValue, configurationEntry);
		}
		if (configurationEntry.isMandatory == null)
		{
			configurationEntry.isMandatory = mandatory;
		}
		else
		{
			configurationEntry.isMandatory = Boolean.valueOf(configurationEntry.isMandatory.booleanValue() || mandatory.booleanValue());
		}
		configurationEntry.setDefaultValue(defaultValue);
		configurationEntry.usedInTypes.add(typeDescr.getFullTypeName());
		if (isJava)
		{
			configurationEntry.inJava = true;
		}
		else
		{
			configurationEntry.inCSharp = true;
		}
	}

	private void handleConfigurationConstants(TypeDescription typeDescr, boolean isJava, HashMap<String, ConfigurationEntry> nameToConfigurationEntryMap)
	{
		for (FieldDescription field : typeDescr.getFieldDescriptions())
		{
			ConfigurationEntry configurationEntry = getEnsureConfigurationEntry(typeDescr, field, nameToConfigurationEntryMap);
			if (configurationEntry == null)
			{
				continue;
			}
			if (isJava)
			{
				configurationEntry.inJava = true;
			}
			else
			{
				configurationEntry.inCSharp = true;
			}
		}
	}

	protected String buildTexPropertyName(ConfigurationEntry propertyEntry)
	{
		String texName = propertyEntry.propertyName;
		while (texName.contains("."))
		{
			int dotIndex = texName.indexOf('.');
			texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1)) + texName.substring(dotIndex + 2);
		}
		while (texName.contains("-"))
		{
			int dotIndex = texName.indexOf('-');
			texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1)) + texName.substring(dotIndex + 2);
		}
		while (texName.contains("_"))
		{
			int dotIndex = texName.indexOf('_');
			texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1)) + texName.substring(dotIndex + 2);
		}
		return Character.toUpperCase(texName.charAt(0)) + texName.substring(1);
	}

	protected ConfigurationEntry getEnsureConfigurationEntry(TypeDescription typeDescr, FieldDescription field, Map<String, ConfigurationEntry> map)
	{
		if (!"java.lang.String".equals(field.getFieldType()) && !"string".equals(field.getFieldType()))
		{
			// we are only interested in configuration constants (strings)
			return null;
		}
		List<String> modifiers = field.getModifiers();
		if (!modifiers.contains("public") || !modifiers.contains("static") || !modifiers.contains("final"))
		{
			// only public static final fields can be constants
			return null;
		}
		String valueOfConstant = field.getInitialValue();
		ConfigurationEntry propertyEntry = map.get(valueOfConstant);
		if (propertyEntry == null)
		{
			propertyEntry = new ConfigurationEntry(valueOfConstant);
			map.put(valueOfConstant, propertyEntry);
		}
		propertyEntry.contantDefinitions.add(typeDescr.getFullTypeName() + "." + field.getName());
		return propertyEntry;
	}

	protected void writeToConfigurationTexFile(ConfigurationEntry propertyEntry, String labelName, File targetFile) throws Exception
	{
		String targetOpening;
		if (propertyEntry.inJava())
		{
			if (propertyEntry.inCSharp())
			{
				targetOpening = availableInJavaAndCsharpOpening;
			}
			else
			{
				targetOpening = availableInJavaOnlyOpening;
			}
		}
		else if (propertyEntry.inCSharp())
		{
			targetOpening = availableInCsharpOnlyOpening;
		}
		else
		{
			throw new IllegalStateException("Neither Java nor C# ?");
		}
		if (!targetFile.exists())
		{
			FileWriter fw = new FileWriter(targetFile);
			try
			{
				fw.append(targetOpening);
				fw.append("\n\\section{").append(propertyEntry.propertyName).append("}");
				fw.append("\n\\label{").append(labelName).append("}");
				fw.append("\n\\ClearAPI");
				fw.append("\n\\TODO");
				fw.append("\n\\begin{lstlisting}[style=Props,caption={Usage example for \\textit{").append(propertyEntry.propertyName).append("}}]");
				fw.append("\n").append(propertyEntry.propertyName).append("=");
				if (propertyEntry.defaultValueSpecified)
				{
					fw.append(propertyEntry.getDefaultValue());
				}
				fw.append("\n\\end{lstlisting}");
			}
			finally
			{
				fw.close();
			}
			targetFile.setLastModified(currentTime);
			return;
		}
		StringBuilder sb = readFileFully(targetFile);
		String newContent = replaceAllAvailables.matcher(sb).replaceAll(Matcher.quoteReplacement(targetOpening));
		if (newContent.contentEquals(sb))
		{
			return;
		}
		updateFileFully(targetFile, newContent);
	}

	protected void writeTableRow(ConfigurationEntry propertyEntry, String labelName, FileWriter fw) throws Exception
	{
		fw.append("\t\t");

		// property name
		fw.append("\\nameref{").append(labelName).append('}');
		fw.append(" & ");

		// default Value
		fw.append(propertyEntry.isDefaultValueSpecified() ? propertyEntry.getDefaultValue() : "");
		fw.append(" & ");

		// mandatory
		fw.append(propertyEntry.isMandatory ? "X" : "");
		fw.append(" & ");

		// Java
		fw.append(propertyEntry.inJava ? "X" : " ").append(" & ");

		// C#
		fw.append(propertyEntry.inCSharp ? "X" : " ");
	}
}
