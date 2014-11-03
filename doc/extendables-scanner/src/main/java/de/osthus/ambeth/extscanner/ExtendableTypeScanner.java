package de.osthus.ambeth.extscanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.MethodDescription;
import de.osthus.classbrowser.java.TypeDescription;

public class ExtendableTypeScanner extends AbstractLatexScanner implements IStartingBean
{
	public static final String LISTING_START = "%% GENERATED LISTINGS - DO NOT EDIT\n";

	public static final String LISTING_END = "%% GENERATED LISTINGS END\n";

	public static final Pattern replaceListingsPattern = Pattern.compile("(.*" + Pattern.quote(LISTING_START) + ").*(" + Pattern.quote(LISTING_END) + ".*)",
			Pattern.DOTALL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = "source-path")
	protected String sourcePath;

	@Property(name = "target-tex-file")
	protected String allExtendablesTexFilePath;

	@Property(name = "target-extendable-tex-dir")
	protected String targetExtendableTexDirPath;

	protected void writeToExtendableTexFile(ExtendableEntry extendableEntry, String labelName, File targetFile) throws Exception
	{
		String targetOpening;
		if (extendableEntry.inJava())
		{
			if (extendableEntry.inCSharp())
			{
				targetOpening = availableInJavaAndCsharpOpening;
			}
			else
			{
				targetOpening = availableInJavaOnlyOpening;
			}
		}
		else if (extendableEntry.inCSharp())
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
				fw.append("\\section{").append(extendableEntry.simpleName).append("}\n");
				fw.append("\\label{").append(labelName).append("}\n");
				fw.append(targetOpening);
				writeJavadoc(extendableEntry.fqName, extendableEntry.simpleName, fw);
				fw.append("\n");
				writeJavadoc(extendableEntry.fqExtensionName, extendableEntry.simpleExtensionName, fw);
				fw.append("\n");
				fw.append("\\TODO}\n\n");

				fw.append(LISTING_START);
				fw.append(listingsString(extendableEntry));
				fw.append(LISTING_END);
			}
			finally
			{
				fw.close();
			}
			return;
		}
		String newContent;
		StringBuilder sb = new StringBuilder((int) targetFile.length());
		BufferedReader rd = new BufferedReader(new FileReader(targetFile));
		try
		{
			int oneByte;
			while ((oneByte = rd.read()) != -1)
			{
				sb.append((char) oneByte);
			}
			newContent = replaceAllAvailables.matcher(sb).replaceAll(Matcher.quoteReplacement(targetOpening));
			Matcher matcher = replaceListingsPattern.matcher(newContent);
			if (matcher.matches())
			{
				newContent = matcher.group(1) + listingsString(extendableEntry) + matcher.group(2);
			}
			else
			{
				log.warn("Could not replace listings in '" + targetFile.getPath() + "'");
			}
		}
		finally
		{
			rd.close();
		}
		if (newContent.contentEquals(sb))
		{
			return;
		}
		// update existing file
		FileWriter fw = new FileWriter(targetFile);
		try
		{
			fw.append(newContent);
		}
		finally
		{
			fw.close();
		}
	}

	protected StringBuilder listingsString(ExtendableEntry extendableEntry)
	{
		StringBuilder sb = new StringBuilder();
		if (extendableEntry.javaFile != null)
		{
			sb.append("\\inputjava{Extension point for instances of \\type{").append(extendableEntry.simpleExtensionName).append("}}\n");
			sb.append("{").append(extendableEntry.javaFile).append("}\n");

			sb.append("\\begin{lstlisting}[style=Java,caption={Example to register to the extension point (Java)}]\n");
			sb.append("IBeanContextFactory bcf = ...\n");
			sb.append("IBeanConfiguration myExtension = bcf.registerAnonymousBean...\n");
			sb.append("bcf.link(myExtension).to(").append(extendableEntry.simpleName).append(".class)");
			if (extendableEntry.hasArguments)
			{
				sb.append(".with(...)");
			}
			sb.append(";\n");
			sb.append("\\end{lstlisting}\n");
		}
		if (extendableEntry.csharpFile != null)
		{
			sb.append("\\inputcsharp{Extension point for instances of \\type{").append(extendableEntry.simpleExtensionName).append("}}\n");
			sb.append("{").append(extendableEntry.csharpFile).append("}\n");

			sb.append("\\begin{lstlisting}[style=Csharp,caption={Example to register to the extension point (C\\#)}]\n");
			sb.append("IBeanContextFactory bcf = ...\n");
			sb.append("IBeanConfiguration myExtension = bcf.RegisterAnonymousBean...\n");
			sb.append("bcf.Link(myExtension).To<").append(extendableEntry.simpleName).append(">()");
			if (extendableEntry.hasArguments)
			{
				sb.append(".With(...)");
			}
			sb.append(";\n");
			sb.append("\\end{lstlisting}\n");
		}
		return sb;
	}

	protected boolean writeTableRow(ExtendableEntry extendableEntry, String labelName, FileWriter fw) throws Exception
	{
		fw.append("\t\t");
		fw.append("\\nameref{").append(labelName).append('}');
		// writeJavadoc(extendableType, fw);
		fw.append(" &\n\t\t");
		writeJavadoc(extendableEntry.fqExtensionName, extendableEntry.simpleExtensionName, fw);
		fw.append(" &\n\t\t");
		if (extendableEntry.inJava())
		{
			fw.append("X &\n\t\t");
		}
		else
		{
			fw.append("  &\n\t\t");
		}
		if (extendableEntry.inCSharp())
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

	protected ExtendableEntry getEnsureExtendableEntry(TypeDescription typeDescr, Map<String, ExtendableEntry> map) throws Exception
	{
		if (typeDescr.isDeprecated())
		{
			// we do not document depcrecated APIs
			return null;
		}
		if (!"interface".equals(typeDescr.getTypeType()))
		{
			return null;
		}
		String key = typeDescr.getName().toLowerCase();
		ExtendableEntry extendableEntry = map.get(key);
		if (extendableEntry == null)
		{
			String extensionType = null;
			boolean hasArguments = false;

			for (MethodDescription methodDesc : typeDescr.getMethodDescriptions())
			{
				String methodName = methodDesc.getName();
				if ((methodName.toLowerCase().startsWith("register") || methodName.toLowerCase().startsWith("add"))
						&& methodDesc.getParameterTypes().size() != 0)
				{
					extensionType = methodDesc.getParameterTypes().get(0);
					hasArguments = methodDesc.getParameterTypes().size() > 1;
					break;
				}
			}
			if (extensionType == null)
			{
				log.warn("Could not find extension in '" + typeDescr.getFullTypeName() + "'");
				return null;
			}
			extendableEntry = new ExtendableEntry(typeDescr.getFullTypeName(), extensionType);
			extendableEntry.hasArguments = hasArguments;
			map.put(key, extendableEntry);
		}
		return extendableEntry;
	}

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
			extendableEntry.javaSrc = typeDescr;
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
			extendableEntry.csharpSrc = typeDescr;
		}
		findCorrespondingSourceFiles(nameToExtendableMap);

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

				writeToExtendableTexFile(extendableEntry, labelName, expectedExtendableTexFile);
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

	protected void findCorrespondingSourceFiles(IMap<String, ExtendableEntry> extendableEntries)
	{
		HashMap<String, IFileFoundDelegate> nameToFileFoundDelegates = new HashMap<String, IFileFoundDelegate>();

		for (Entry<String, ExtendableEntry> entry : extendableEntries)
		{
			final ExtendableEntry extendableEntry = entry.getValue();

			queueFileSearch(extendableEntry.javaSrc, ".java", nameToFileFoundDelegates, new IFileFoundDelegate()
			{
				@Override
				public void fileFound(File file, String relativeFilePath)
				{
					extendableEntry.javaFile = relativeFilePath;
				}
			});
			queueFileSearch(extendableEntry.csharpSrc, ".cs", nameToFileFoundDelegates, new IFileFoundDelegate()
			{
				@Override
				public void fileFound(File file, String relativeFilePath)
				{
					extendableEntry.csharpFile = relativeFilePath;
				}
			});
		}

		try
		{
			String[] pathItems = sourcePath.split(";");
			for (String pathItem : pathItems)
			{
				File rootDir = new File(pathItem).getCanonicalFile();
				searchForFiles(rootDir, rootDir, nameToFileFoundDelegates);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void queueFileSearch(TypeDescription typeDesc, String expectedSuffix, IMap<String, IFileFoundDelegate> nameToFileFoundDelegates,
			IFileFoundDelegate fileFoundDelegate)
	{
		if (typeDesc == null)
		{
			return;
		}
		String fileName = typeDesc.getName() + expectedSuffix;
		nameToFileFoundDelegates.put(fileName, fileFoundDelegate);
	}

	protected void searchForFiles(File baseDir, File currFile, IMap<String, IFileFoundDelegate> nameToFileFoundDelegates)
	{
		if (currFile == null)
		{
			return;
		}
		if (currFile.isDirectory())
		{
			File[] listFiles = currFile.listFiles();
			for (File child : listFiles)
			{
				searchForFiles(baseDir, child, nameToFileFoundDelegates);
				if (nameToFileFoundDelegates.size() == 0)
				{
					return;
				}
			}
			return;
		}
		IFileFoundDelegate fileFoundDelegate = nameToFileFoundDelegates.remove(currFile.getName());
		if (fileFoundDelegate == null)
		{
			return;
		}
		String relativeFilePath = currFile.getPath().substring(baseDir.getPath().length() + 1).replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement("/"));
		fileFoundDelegate.fileFound(currFile, relativeFilePath);
	}
}
