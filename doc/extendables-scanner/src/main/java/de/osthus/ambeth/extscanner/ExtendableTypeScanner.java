package de.osthus.ambeth.extscanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javassist.CtClass;
import javassist.CtMethod;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.MethodDescription;
import de.osthus.classbrowser.java.TypeDescription;

public class ExtendableTypeScanner extends AbstractLatexScanner implements IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = "target-tex-file")
	protected String allExtendablesTexFilePath;

	@Property(name = "target-extendable-tex-dir")
	protected String targetExtendableTexDirPath;

	// protected void processNamespaceDefinition(File file, String namespace, ScopedSource source, HashMap<String, ExtendableEntry> nameToExtendableMap)
	// {
	// for (int a = 0, size = source.size(); a < size; a++)
	// {
	// Object obj = source.get(a);
	// if (!(obj instanceof String))
	// {
	// continue;
	// }
	// Matcher matcher = csharpExtendableNamePattern.matcher(obj.toString());
	// if (matcher.matches())
	// {
	// processExtendableDefinition(file, namespace, matcher.group(1), (ScopedSource) source.get(a + 1), nameToExtendableMap);
	// }
	// }
	// }
	//
	// protected void processExtendableDefinition(File file, String namespace, String simpleExtendableName, ScopedSource source,
	// HashMap<String, ExtendableEntry> nameToExtendableMap)
	// {
	// String extendableName = namespace + "." + simpleExtendableName;
	//
	// try
	// {
	// BufferedReader rd = new BufferedReader(new StringReader(source.toString()));
	// String line;
	// while ((line = rd.readLine()) != null)
	// {
	// Matcher matcher = methodPattern.matcher(line);
	// if (!matcher.matches())
	// {
	// continue;
	// }
	// String visibility = matcher.group(1);
	// String returnType = matcher.group(2);
	// String methodName = matcher.group(3);
	// String parameters = matcher.group(4);
	//
	// try
	// {
	// String[] parameterItems = parameterSplitPattern.split(parameters);
	// String[] parameterTypes = new String[parameterItems.length];
	// for (int a = 0, size = parameterItems.length; a < size; a++)
	// {
	// String parameterItem = parameterItems[a];
	// Matcher parameterMatcher = parameterPattern.matcher(parameterItem);
	// if (!parameterMatcher.matches())
	// {
	// throw new IllegalStateException("Could not parse parameter: '" + parameterItem + "'");
	// }
	// String parameterType = parameterMatcher.group(1);
	// String parameterName = parameterMatcher.group(2);
	// parameterTypes[a] = parameterType;
	// }
	//
	// ExtendableEntry extendableEntry = nameToExtendableMap.get(extendableName);
	// if (extendableEntry == null)
	// {
	// CtClass extensionType = null;
	//
	// CtMethod[] methods = extendableType.getMethods();
	// for (int a = methods.length; a-- > 0;)
	// {
	// String methodName = methods[a].getName();
	// if ((methodName.toLowerCase().startsWith("register") || methodName.toLowerCase().startsWith("add"))
	// && methods[a].getParameterTypes().length >= 1)
	// {
	// extensionType = methods[a].getParameterTypes()[0];
	// break;
	// }
	// }
	// if (extensionType == null)
	// {
	// log.warn("Could not find extension in '" + extendableType.getName() + "'");
	// return null;
	// }
	// extendableEntry = new ExtendableEntry(extendableName, extensionName);
	// nameToExtendableMap.put(extendableName, extendableEntry);
	// }
	// extendableEntry.inJava = true;
	// }
	// catch (Throwable e)
	// {
	// throw RuntimeExceptionUtil
	// .mask(e, "Exception occurred while processing method '" + returnType + " " + methodName + "(" + parameters + ")'");
	// }
	// }
	// }
	// catch (Throwable e)
	// {
	// throw RuntimeExceptionUtil.mask(e, "Exception occurred while processing extendable '" + namespace + "." + simpleExtendableName + "'");
	// }
	// }

	protected void writeEmptyExtendableTexFile(ExtendableEntry extendableEntry, String labelName, File targetFile) throws Exception
	{
		FileWriter fw = new FileWriter(targetFile);
		try
		{
			fw.append("\\section{").append(extendableEntry.simpleName).append("}\n");
			fw.append("\\label{").append(labelName).append("}\n");
			if (extendableEntry.inJava)
			{
				if (extendableEntry.inCSharp)
				{
					fw.append("\\AvailableInJavaAndCsharp{");
				}
				else
				{
					fw.append("\\AvailableInJavaOnly{");
				}
			}
			else if (extendableEntry.inCSharp)
			{
				fw.append("\\AvailableInCsharpOnly{");
			}
			writeJavadoc(extendableEntry.fqName, extendableEntry.simpleName, fw);
			fw.append("\n");
			writeJavadoc(extendableEntry.fqExtensionName, extendableEntry.simpleExtensionName, fw);
			fw.append("\n");
			fw.append("\\TODO}");
		}
		finally
		{
			fw.close();
		}
	}

	protected boolean writeTableRow(ExtendableEntry extendableEntry, String labelName, FileWriter fw) throws Exception
	{
		fw.append("\t\t");
		fw.append("\\nameref{").append(labelName).append('}');
		// writeJavadoc(extendableType, fw);
		fw.append(" &\n\t\t");
		writeJavadoc(extendableEntry.fqExtensionName, extendableEntry.simpleExtensionName, fw);
		fw.append(" &\n\t\t");
		if (extendableEntry.inJava)
		{
			fw.append("X &\n\t\t");
		}
		else
		{
			fw.append("  &\n\t\t");
		}
		if (extendableEntry.inCSharp)
		{
			fw.append("X ");
		}
		else
		{
			fw.append("  ");
		}
		// fw.append("\n\t\t");
		// fw.append("\\shortprettyref{").append(labelName).append('}');
		return true;
	}

	protected void writeJavadoc(String fqName, String simpleName, FileWriter fw) throws IOException
	{
		fw.append("\\javadoc{").append(fqName).append("}{").append(simpleName).append('}');
	}

	protected ExtendableEntry getEnsureExtendableEntry(CtClass extendableType, Map<String, ExtendableEntry> map) throws Exception
	{
		ExtendableEntry extendableEntry = map.get(extendableType.getName());
		if (extendableEntry == null)
		{
			CtClass extensionType = null;

			CtMethod[] methods = extendableType.getMethods();
			for (int a = methods.length; a-- > 0;)
			{
				String methodName = methods[a].getName();
				if ((methodName.toLowerCase().startsWith("register") || methodName.toLowerCase().startsWith("add"))
						&& methods[a].getParameterTypes().length >= 1)
				{
					extensionType = methods[a].getParameterTypes()[0];
					break;
				}
			}
			if (extensionType == null)
			{
				log.warn("Could not find extension in '" + extendableType.getName() + "'");
				return null;
			}
			extendableEntry = new ExtendableEntry(extendableType.getName(), extensionType.getName());
			map.put(extendableType.getName(), extendableEntry);
		}
		extendableEntry.inJava = true;
		return extendableEntry;
	}

	protected ExtendableEntry getEnsureExtendableEntry(TypeDescription typeDescr, Map<String, ExtendableEntry> map) throws Exception
	{
		ExtendableEntry extendableEntry = map.get(typeDescr.getFullTypeName());
		if (extendableEntry == null)
		{
			String extensionType = null;

			for (MethodDescription methodDesc : typeDescr.getMethodDescriptions())
			{
				String methodName = methodDesc.getName();
				if ((methodName.toLowerCase().startsWith("register") || methodName.toLowerCase().startsWith("add"))
						&& methodDesc.getParameterTypes().size() != 0)
				{
					extensionType = methodDesc.getParameterTypes().get(0);
					break;
				}
			}
			if (extensionType == null)
			{
				log.warn("Could not find extension in '" + typeDescr.getFullTypeName() + "'");
				return null;
			}
			extendableEntry = new ExtendableEntry(typeDescr.getFullTypeName(), extensionType);
			map.put(typeDescr.getFullTypeName(), extendableEntry);
		}
		return extendableEntry;
	}

	// protected ExtendableEntry getEnsureExtendableEntry(BufferedReader rd, String fqExtendableName, Map<String, ExtendableEntry> map) throws Exception
	// {
	// ExtendableEntry extendableEntry = map.get(fqExtendableName);
	// if (extendableEntry == null)
	// {
	// String fqExtensionName = null;
	//
	// int bracketCounter = 0;
	// String line;
	// while ((line = rd.readLine()) != null)
	// {
	// if (bracketCounter == 0)
	// {
	// // look for first bracket
	// if (line.contains("{"))
	// {
	// line
	// }
	// }
	// }
	//
	// CtMethod[] methods = extendableType.getMethods();
	// for (int a = methods.length; a-- > 0;)
	// {
	// String methodName = methods[a].getName();
	// if ((methodName.toLowerCase().startsWith("register") || methodName.toLowerCase().startsWith("add"))
	// && methods[a].getParameterTypes().length >= 1)
	// {
	// extensionType = methods[a].getParameterTypes()[0];
	// break;
	// }
	// }
	// extendableEntry = new ExtendableEntry(extendableType.getName(), extensionType.getName());
	// map.put(fqExtendableName, extendableEntry);
	// }
	// extendableEntry.inCSharp = true;
	// return extendableEntry;
	// }

	@Override
	protected void handle(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable
	{
		File allExtendablesTexFile = new File(allExtendablesTexFilePath).getCanonicalFile();
		allExtendablesTexFile.getParentFile().mkdirs();

		File targetExtendableTexDir = new File(targetExtendableTexDirPath).getCanonicalFile();
		targetExtendableTexDir.mkdirs();

		log.info("TargetTexFile: " + allExtendablesTexFile);
		log.info("ExtendableTexDir: " + targetExtendableTexDir);

		String targetExtendableTexDirCP = targetExtendableTexDir.getPath();
		String targetTexFileCP = allExtendablesTexFile.getParent();
		if (!targetExtendableTexDirCP.startsWith(targetTexFileCP))
		{
			throw new IllegalStateException("Path '" + targetExtendableTexDirCP + "' must reside within '" + targetTexFileCP + "'");
		}
		String pathToExtendableTexFile = targetExtendableTexDirCP.substring(targetTexFileCP.length() + 1);

		HashMap<String, ExtendableEntry> nameToExtendableMap = new HashMap<String, ExtendableEntry>();

		for (Entry<String, TypeDescription> entry : javaTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			if (!typeDescr.getName().endsWith("Extendable"))
			{
				continue;
			}
			ExtendableEntry extendableEntry = getEnsureExtendableEntry(typeDescr, nameToExtendableMap);
			if (extendableEntry == null)
			{
				continue;
			}
			extendableEntry.inJava = true;
		}

		for (Entry<String, TypeDescription> entry : csharpTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			if (!typeDescr.getName().endsWith("Extendable"))
			{
				continue;
			}
			ExtendableEntry extendableEntry = getEnsureExtendableEntry(typeDescr, nameToExtendableMap);
			if (extendableEntry == null)
			{
				continue;
			}
			extendableEntry.inCSharp = true;
		}

		String[] extendableNames = nameToExtendableMap.keySet().toArray(String.class);
		java.util.Arrays.sort(extendableNames);

		FileWriter fw = new FileWriter(allExtendablesTexFile);
		try
		{
			fw.append("%---------------------------------------------------------------\n");
			fw.append("% This file is FULLY generated. Please do not edit anything here\n");
			fw.append("% Any changes have to be done to the java class " + ExtendableTypeScanner.class.getName() + "\n");
			fw.append("%---------------------------------------------------------------\n");
			fw.append("\\chapter{Ambeth Extension Points}\n");
			fw.append("\\begin{longtable}{ l l c c } \\hline \\textbf{Extension Point} & \\textbf{Extension} & \\textbf{Java} & \\textbf{C\\#} \\\n");
			fw.append("\t\\endhead\n");
			fw.append("\t\\hline\n");

			ArrayList<String> includes = new ArrayList<String>();

			for (String extendableName : extendableNames)
			{
				ExtendableEntry extendableEntry = nameToExtendableMap.get(extendableName);
				log.info("Handling " + extendableEntry.fqName);
				String texName = extendableEntry.simpleName;

				String labelName = "extendable:" + texName;
				if (!writeTableRow(extendableEntry, labelName, fw))
				{
					continue;
				}
				fw.append(" \\\\\n");
				fw.append("\t\\hline\n");

				String expectedExtendableTexFileName = texName + ".tex";

				includes.add(pathToExtendableTexFile + "/" + texName);

				File expectedExtendableTexFile = new File(targetExtendableTexDir, expectedExtendableTexFileName);

				if (!expectedExtendableTexFile.exists())
				{
					writeEmptyExtendableTexFile(extendableEntry, labelName, expectedExtendableTexFile);
				}
			}
			fw.append("\\end{longtable}\n");
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
	}
}
