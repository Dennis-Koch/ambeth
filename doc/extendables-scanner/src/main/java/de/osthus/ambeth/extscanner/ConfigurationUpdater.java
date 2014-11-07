package de.osthus.ambeth.extscanner;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.FieldDescription;
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

		HashMap<String, ConfigurationEntry> nameToPropertyMap = new HashMap<String, ConfigurationEntry>();

		for (Entry<String, TypeDescription> entry : javaTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			if (!typeDescr.getName().endsWith("ConfigurationConstants"))
			{
				continue;
			}
			handleConfigurationConstants(typeDescr, true, nameToPropertyMap);
		}

		for (Entry<String, TypeDescription> entry : csharpTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			if (!typeDescr.getName().endsWith("ConfigurationConstants"))
			{
				continue;
			}
			handleConfigurationConstants(typeDescr, false, nameToPropertyMap);
		}

		String[] propertyNames = nameToPropertyMap.keySet().toArray(String.class);
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
				ConfigurationEntry propertyEntry = nameToPropertyMap.get(propertyName);
				log.info("Handling " + propertyEntry.propertyName);

				String texName = buildTexPropertyName(propertyEntry);

				String labelName = "configuration:" + texName;
				writeTableRow(propertyEntry, labelName, fw);
				fw.append(" \\\\\n");
				fw.append("\t\\hline\n");

				String expectedConfigurationTexFileName = texName + ".tex";

				includes.add(pathToExtendableTexFile + "/" + texName);

				File expectedConfigurationTexFile = new File(targetPropertiesTexDir, expectedConfigurationTexFileName);

				if (!expectedConfigurationTexFile.exists())
				{
					writeEmptyConfigurationTexFile(propertyEntry, labelName, expectedConfigurationTexFile);
					// nameToConfigurationConstantsMap.get(propertyEntry.propertyName));
				}
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

		// for (String propertyName : nameToPropertyMap.keySet())
		// {
		// // remove all "used" properties
		// nameToConfigurationConstantsMap.remove(propertyName);
		// }
		// if (nameToConfigurationConstantsMap.size() > 0)
		// {
		// String[] obsoletePropertyNames = nameToConfigurationConstantsMap.keySet().toArray(String.class);
		// Arrays.sort(obsoletePropertyNames);
		// StringBuilder sb = new StringBuilder();
		// sb.append("There are ").append(obsoletePropertyNames.length).append(" configurations without any usage and were therefore been skipped:");
		// for (String obsoletePropertyName : obsoletePropertyNames)
		// {
		// CtField ctField = nameToConfigurationConstantsMap.get(obsoletePropertyName);
		// sb.append('\n').append(obsoletePropertyName).append(" ==> ").append(ctField.getDeclaringClass().getName()).append(".")
		// .append(ctField.getName());
		// }
		// log.warn(sb.toString());
		// }
	}

	private void handleConfigurationConstants(TypeDescription typeDescr, boolean isJava, HashMap<String, ConfigurationEntry> nameToConfigurationEntryMap)
	{
		for (FieldDescription field : typeDescr.getFieldDescriptions())
		{
			ConfigurationEntry configurationEntry = getEnsureConfigurationEntry(field, nameToConfigurationEntryMap);
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

	protected ConfigurationEntry getEnsureConfigurationEntry(FieldDescription field, Map<String, ConfigurationEntry> map)
	{
		if (!field.getFieldType().equals("java.lang.String"))
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
		String valueOfConstant = field.getName() + "$VALUE";
		ConfigurationEntry propertyEntry = map.get(valueOfConstant);
		if (propertyEntry == null)
		{
			propertyEntry = new ConfigurationEntry(valueOfConstant);
			map.put(valueOfConstant, propertyEntry);
		}
		return propertyEntry;
	}

	protected void writeEmptyConfigurationTexFile(ConfigurationEntry propertyEntry, String labelName, File targetFile) throws Exception
	{
		FileWriter fw = new FileWriter(targetFile);
		try
		{
			fw.append("\\section{").append(propertyEntry.propertyName).append("}\n");
			fw.append("\\label{").append(labelName).append("}\n");
			if (propertyEntry.inJava)
			{
				if (propertyEntry.inCSharp)
				{
					fw.append("\\AvailableInJavaAndCsharp{\\TODO}");
				}
				else
				{
					fw.append("\\AvailableInJavaOnly{\\TODO}");
				}
			}
			else if (propertyEntry.inCSharp)
			{
				fw.append("\\AvailableInCsharpOnly{\\TODO}");
			}
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
