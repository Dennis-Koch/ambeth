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
import de.osthus.classbrowser.java.TypeDescription;

public class AnnotationUpdater extends AbstractLatexScanner implements IStartingBean
{
	public static final Pattern cutSuffixPattern = Pattern.compile("(.+)Attribute");

	public static final String LISTING_START = "%% GENERATED LISTINGS - DO NOT EDIT";

	public static final String LISTING_END = "%% GENERATED LISTINGS END";

	public static final Pattern replaceListingsPattern = Pattern.compile("(.*" + Pattern.quote(LISTING_START) + ").*(" + Pattern.quote(LISTING_END) + ".*)",
			Pattern.DOTALL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IModel model;

	@Property(name = "target-annotation-tex-dir")
	protected String targetAnnotationTexDirPath;

	protected void writeToAnnotationTexFile(AnnotationEntry annotationEntry, File targetFile) throws Exception
	{
		String targetOpening = getAPI(annotationEntry);
		if (!targetFile.exists())
		{
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(targetFile), Charset.forName("UTF-8"));
			try
			{
				fw.append(targetOpening);
				fw.append("\n\\section{").append(annotationEntry.simpleName).append("}");
				fw.append("\n\\label{").append(annotationEntry.labelName).append("}");
				fw.append("\n\\ClearAPI\n");
				writeJavadoc(annotationEntry.annotationName, annotationEntry.simpleName, fw);
				fw.append("\n\\TODO\n\n");

				fw.append(LISTING_START).append('\n');
				fw.append(listingsString(annotationEntry));
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
		String newContent = writeSetAPI(sb, annotationEntry);
		Matcher matcher = replaceListingsPattern.matcher(newContent);
		if (matcher.matches())
		{
			newContent = matcher.group(1) + "\n" + listingsString(annotationEntry) + matcher.group(2);
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

	protected StringBuilder listingsString(AnnotationEntry annotationEntry)
	{
		StringBuilder sb = new StringBuilder();
		if (annotationEntry.javaFile != null)
		{
			sb.append("\\inputjava{Annotation definition \\type{").append(annotationEntry.simpleName).append("}}\n");
			sb.append("{").append(annotationEntry.javaFile).append("}\n");
		}
		if (annotationEntry.csharpFile != null)
		{
			sb.append("\\inputcsharp{Annotation definition \\type{").append(annotationEntry.simpleName).append("}}\n");
			sb.append("{").append(annotationEntry.csharpFile).append("}\n");
		}
		return sb;
	}

	protected boolean writeTableRow(AnnotationEntry annotationEntry, OutputStreamWriter fw) throws Exception
	{
		fw.append("\t\t");
		fw.append("\\nameref{").append(annotationEntry.labelName).append('}');
		fw.append(" &\n\t\t");
		writeAvailability(annotationEntry, fw);
		return true;
	}

	protected AnnotationEntry getEnsureAnnotationEntry(TypeDescription typeDescr) throws Exception
	{
		if (typeDescr.isDeprecated())
		{
			// we do not document depcrecated APIs
			return null;
		}
		String annotationName = typeDescr.getName();
		Matcher matcher = cutSuffixPattern.matcher(annotationName);
		if (matcher.matches())
		{
			annotationName = matcher.group(1);
		}
		AnnotationEntry annotationEntry = model.resolveAnnotation(annotationName);
		if (annotationEntry == null)
		{
			String fqName = typeDescr.getFullTypeName();
			String simpleName = typeDescr.getName();
			matcher = cutSuffixPattern.matcher(simpleName);
			if (matcher.matches())
			{
				simpleName = matcher.group(1);
				annotationName = simpleName.toLowerCase();

				matcher = cutSuffixPattern.matcher(fqName);
				if (!matcher.matches())
				{
					throw new IllegalStateException("Must never happen");
				}
				fqName = matcher.group(1);
			}
			String texName = simpleName;
			String labelName = "annotation:" + texName;
			annotationEntry = new AnnotationEntry(fqName, simpleName, labelName, typeDescr.getModuleName());
			model.addAnnotation(annotationName, annotationEntry);
		}
		return annotationEntry;
	}

	@Override
	protected void buildModel(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable
	{
		for (Entry<String, TypeDescription> entry : javaTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			boolean isAnnotation = false;
			for (String interfaceName : typeDescr.getInterfaces())
			{
				if ("java.lang.annotation.Annotation".equals(interfaceName))
				{
					isAnnotation = true;
					break;
				}
			}
			if (!isAnnotation)
			{
				continue;
			}
			AnnotationEntry annotationEntry = getEnsureAnnotationEntry(typeDescr);
			if (annotationEntry == null)
			{
				continue;
			}
			annotationEntry.javaSrc = typeDescr;
		}

		for (Entry<String, TypeDescription> entry : csharpTypes)
		{
			TypeDescription typeDescr = entry.getValue();
			boolean isAnnotation = false;
			for (String interfaceName : typeDescr.getInterfaces())
			{
				if ("System.Runtime.InteropServices._Attribute".equals(interfaceName))
				{
					isAnnotation = true;
					break;
				}
			}
			if (!isAnnotation)
			{
				continue;
			}
			AnnotationEntry annotationEntry = getEnsureAnnotationEntry(typeDescr);
			if (annotationEntry == null)
			{
				continue;
			}
			annotationEntry.csharpSrc = typeDescr;
		}
		findCorrespondingSourceFiles(model.allAnnotations());
	}

	@Override
	protected void handleModel() throws Throwable
	{
		File allAnnotationsTexFile = new File(getAllDir(), "all-annotations.tex");

		File targetTexDir = new File(targetAnnotationTexDirPath).getCanonicalFile();
		targetTexDir.mkdirs();

		log.debug("TargetTexFile: " + allAnnotationsTexFile);
		log.debug("ExtendableTexDir: " + targetTexDir);

		String targetTexDirCP = targetTexDir.getPath();
		String targetTexFileCP = allAnnotationsTexFile.getParent();
		if (!targetTexDirCP.startsWith(targetTexFileCP))
		{
			throw new IllegalStateException("Path '" + targetTexDirCP + "' must reside within '" + targetTexFileCP + "'");
		}
		String pathToTexFile = targetTexDirCP.substring(targetTexFileCP.length() + 1);

		ArrayList<AnnotationEntry> allAnnotations = new ArrayList<AnnotationEntry>(model.allAnnotations());
		Collections.sort(allAnnotations);

		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(allAnnotationsTexFile), Charset.forName("UTF-8"));
		try
		{
			fw.append("%---------------------------------------------------------------\n");
			fw.append("% This file is FULLY generated. Please do not edit anything here\n");
			fw.append("% Any changes have to be done to the java class " + AnnotationUpdater.class.getName() + "\n");
			fw.append("%---------------------------------------------------------------\n");
			fw.append("\\chapter{Ambeth Annotations}\n");
			fw.append("\\label{ambeth:annotations}\n");
			fw.append("\\begin{longtable}{ l c c c } \\hline \\textbf{Annotation} & \\textbf{Java} & \\textbf{C\\#} & \\textbf{Javascript} \\\n");
			fw.append("\t\\endhead\n");
			fw.append("\t\\hline\n");

			ArrayList<String> includes = new ArrayList<String>();

			for (AnnotationEntry annotationEntry : allAnnotations)
			{
				log.debug("Handling " + annotationEntry.annotationName);
				String texName = annotationEntry.simpleName;

				if (!writeTableRow(annotationEntry, fw))
				{
					continue;
				}
				fw.append(" \\\\\n");
				fw.append("\t\\hline\n");

				String expectedAnnotationTexFileName = texName + ".tex";

				includes.add(pathToTexFile + "/" + texName);

				File expectedExtendableTexFile = new File(targetTexDir, expectedAnnotationTexFileName);

				writeToAnnotationTexFile(annotationEntry, expectedExtendableTexFile);
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
		allAnnotationsTexFile.setLastModified(currentTime);
	}
}
