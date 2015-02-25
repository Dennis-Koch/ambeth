package de.osthus.ambeth.extscanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.classbrowser.java.MethodDescription;
import de.osthus.classbrowser.java.TypeDescription;

public class ExtendableUpdater extends AbstractLatexScanner implements IStartingBean
{
	public static final String LISTING_START = "%% GENERATED LISTINGS - DO NOT EDIT";

	public static final String LISTING_END = "%% GENERATED LISTINGS END";

	public static final Pattern replaceListingsPattern = Pattern.compile("(.*" + Pattern.quote(LISTING_START) + ").*(" + Pattern.quote(LISTING_END) + ".*)",
			Pattern.DOTALL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IModel model;

	@Property(name = "target-extendable-tex-dir")
	protected String targetExtendableTexDirPath;

	protected void writeToExtendableTexFile(ExtendableEntry extendableEntry, String labelName, File targetFile) throws Exception
	{
		String targetOpening = getAPI(extendableEntry);
		if (!targetFile.exists())
		{
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(targetFile), Charset.forName("UTF-8"));
			try
			{
				fw.append(targetOpening);
				fw.append("\n\\section{").append(extendableEntry.simpleName).append("}");
				fw.append("\n\\label{").append(labelName).append("}");
				fw.append("\n\\ClearAPI\n");
				writeJavadoc(extendableEntry.fqName, extendableEntry.simpleName, fw);
				fw.append("\n");
				writeJavadoc(extendableEntry.fqExtensionName, extendableEntry.simpleExtensionName, fw);
				fw.append("\n\\TODO\n\n");

				fw.append(LISTING_START).append('\n');
				fw.append(listingsString(extendableEntry));
				fw.append(LISTING_END);
			}
			finally
			{
				fw.close();
			}
			targetFile.setLastModified(currentTime);
			return;
		}
		StringBuilder sb = readFileFully(targetFile);
		String newContent = writeSetAPI(sb, extendableEntry);
		Matcher matcher = replaceListingsPattern.matcher(newContent);
		if (matcher.matches())
		{
			newContent = matcher.group(1) + "\n" + listingsString(extendableEntry) + matcher.group(2);
		}
		else
		{
			log.warn("Could not replace listings in '" + targetFile.getPath() + "'");
		}
		if (newContent.contentEquals(sb))
		{
			return;
		}
		updateFileFully(targetFile, newContent);
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

	protected boolean writeTableRow(ExtendableEntry extendableEntry, String labelName, OutputStreamWriter fw) throws Exception
	{
		fw.append("\t\t");
		fw.append("\\nameref{").append(labelName).append('}');
		// writeJavadoc(extendableType, fw);
		fw.append(" &\n\t\t");
		writeJavadoc(extendableEntry.fqExtensionName, extendableEntry.simpleExtensionName, fw);
		fw.append(" &\n\t\t");

		writeAvailability(extendableEntry, fw);
		return true;
	}

	protected ExtendableEntry getEnsureExtendableEntry(TypeDescription typeDescr) throws Exception
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
		String extendableName = typeDescr.getName().toLowerCase();
		ExtendableEntry extendableEntry = model.resolveExtendable(extendableName);
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
			Matcher matcher = AbstractSourceFileAware.pattern.matcher(typeDescr.getFullTypeName());
			if (!matcher.matches())
			{
				throw new IllegalArgumentException(typeDescr.getFullTypeName());
			}
			String simpleName = matcher.group(1);
			String texName = simpleName;
			String labelName = "extendable:" + texName;
			extendableEntry = new ExtendableEntry(typeDescr.getFullTypeName(), simpleName, labelName, extensionType);
			extendableEntry.hasArguments = hasArguments;
			model.addExtendable(extendableName, extendableEntry);
		}
		return extendableEntry;
	}

	@Override
	protected void buildModel(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable
	{
		for (Entry<String, TypeDescription> entry : javaTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			if (!typeDescr.getName().endsWith("Extendable"))
			{
				continue;
			}
			ExtendableEntry extendableEntry = getEnsureExtendableEntry(typeDescr);
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
			ExtendableEntry extendableEntry = getEnsureExtendableEntry(typeDescr);
			if (extendableEntry == null)
			{
				continue;
			}
			extendableEntry.csharpSrc = typeDescr;
		}
		findCorrespondingSourceFiles(model.allExtendables());
	}

	@Override
	protected void handleModel() throws Throwable
	{
		File allExtendablesTexFile = new File(getAllDir(), "all-extendables.tex");

		File targetExtendableTexDir = new File(targetExtendableTexDirPath).getCanonicalFile();
		targetExtendableTexDir.mkdirs();

		log.debug("TargetTexFile: " + allExtendablesTexFile);
		log.debug("ExtendableTexDir: " + targetExtendableTexDir);

		String targetExtendableTexDirCP = targetExtendableTexDir.getPath();
		String targetTexFileCP = allExtendablesTexFile.getParent();
		if (!targetExtendableTexDirCP.startsWith(targetTexFileCP))
		{
			throw new IllegalStateException("Path '" + targetExtendableTexDirCP + "' must reside within '" + targetTexFileCP + "'");
		}
		String pathToExtendableTexFile = targetExtendableTexDirCP.substring(targetTexFileCP.length() + 1);

		ArrayList<ExtendableEntry> allExtendables = new ArrayList<ExtendableEntry>(model.allExtendables());
		Collections.sort(allExtendables);

		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(allExtendablesTexFile), Charset.forName("UTF-8"));
		try
		{
			fw.append("%---------------------------------------------------------------\n");
			fw.append("% This file is FULLY generated. Please do not edit anything here\n");
			fw.append("% Any changes have to be done to the java class " + ExtendableUpdater.class.getName() + "\n");
			fw.append("%---------------------------------------------------------------\n");
			fw.append("\\chapter{Ambeth Extension Points}\n");
			fw.append("\\begin{landscape}\n");
			fw.append("\\begin{longtable}{ l l c c c } \\hline \\textbf{Extension Point} & \\textbf{Extension} & \\textbf{Java} & \\textbf{C\\#} & \\textbf{Javascript} \\\n");
			fw.append("\t\\endhead\n");
			fw.append("\t\\hline\n");

			ArrayList<String> includes = new ArrayList<String>();

			for (ExtendableEntry extendableEntry : allExtendables)
			{
				log.debug("Handling " + extendableEntry.fqName);
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
		allExtendablesTexFile.setLastModified(currentTime);
	}
}